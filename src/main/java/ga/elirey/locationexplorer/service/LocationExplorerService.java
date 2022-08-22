package ga.elirey.locationexplorer.service;

import ga.elirey.locationexplorer.constant.LocationOutputFormat;
import ga.elirey.locationexplorer.data.FilterOptions;
import ga.elirey.locationexplorer.entity.UnitLocationMeasurement;
import ga.elirey.locationexplorer.model.GPSPoint;
import ga.elirey.locationexplorer.model.GPSTrack;
import ga.elirey.locationexplorer.repository.LocationExplorerRepository;
import ga.elirey.locationexplorer.utils.CustomTimer;
import ga.elirey.locationexplorer.utils.GpsJumpsFilterAlgorithm;
import ga.elirey.locationexplorer.utils.RamerDouglasPeuckerAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationExplorerService {

    private final LocationExplorerRepository repository;

    /**
     * Convert a database entry to a geojson or a gpx entry (other location data format will be added progressively)
     *
     * @return a string version of the produced geojson
     */
    @Transactional(readOnly = true)
    public String convert(final String userId, final String missionId, final String outputFormat,
                          final Long startDateMillis, final Long endDateMillis,
                          final FilterOptions filterOptions) throws Exception {

        final FilterOptions options = Optional.ofNullable(filterOptions)
                .orElse(FilterOptions.useDefault());

        final LocalDateTime startDateTime = setStartTimeFromParam(startDateMillis);
        final LocalDateTime endDateTime = setStopTimeFromParam(endDateMillis);
        log.info("Request all locations from => {} to => {} for user =>  '{}' in mission => '{}'", startDateTime, endDateTime, userId, missionId);

        final List<UnitLocationMeasurement> locations = repository.findByIdTimeAfterAndIdTimeBeforeAndIdUnitIdAndIdMissionId(startDateTime, endDateTime, userId, missionId);

        final GPSTrack gpsTrack = loadGpsTrackForUserWithOptimizationParameters(userId, locations,
                options.isPathOptimizerEnabled(), options.getOptimizationCoefficient(), options.isGpsJumpFilterEnabled());
        final LocationOutputFormat format = loadLocationOutputFormatFromParam(outputFormat);
        switch (format) {
            default:
            case GEOJSON:
                final CustomTimer timerGeoJsonOperation = new CustomTimer();
                log.info("Start GEOJSON transformation...");
                final String geojson = gpsTrack.asGeoJsonString(options.isWayPointIncluded()); // Refactor more intelligently
                log.info("Processed {} location points to GEOJSON format in {} ms!", gpsTrack.size(), timerGeoJsonOperation.elapsedMsecs());
                return geojson;
            case GPX:
                final CustomTimer timerGpxOperation = new CustomTimer();
                log.info("Start GPX transformation...");
                final String gpx = gpsTrack.asGpxString();
                log.info("Processed {} location points to GPX format in {} ms!", gpsTrack.size(), timerGpxOperation.elapsedMsecs());
                return gpx;
        }
    }

    private List<GPSPoint> optimizePath(final List<GPSPoint> points, final boolean isPathOptimizerEnabled, final int optimizationCoefficient) {
        if (isPathOptimizerEnabled) {
            log.trace("Processing {} points with Ramer-Douglas-Peucker algorithm", points.size());
            final RamerDouglasPeuckerAlgorithm<GPSPoint> rdp = new RamerDouglasPeuckerAlgorithm<>(optimizationCoefficient);
            return rdp.apply(points);
        } else return points; // do nothing
    }

    private List<GPSPoint> filterGpsJumps(final List<GPSPoint> points, final boolean isGpsJumpFilterEnabled) {
        if (isGpsJumpFilterEnabled) {
            GpsJumpsFilterAlgorithm<GPSPoint> gjf = new GpsJumpsFilterAlgorithm<>();
            return gjf.apply(points);
        } else return points;
    }

    private LocalDateTime setStartTimeFromParam(final Long startDateMillis) {
        final LocalDateTime startDateTime = (startDateMillis == null || startDateMillis == 0) ?
                LocalDate.now().minus(10, ChronoUnit.DAYS).atStartOfDay() : // get last 10 days locations
                Instant.ofEpochMilli(startDateMillis).atZone(ZoneId.of("UTC")).toLocalDateTime();
        log.trace(String.format("Start traces at => '%s'", startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        return startDateTime;
    }

    private LocalDateTime setStopTimeFromParam(final Long endDateMillis) {
        final LocalDateTime endDateTime = (endDateMillis == null || endDateMillis == 0) ?
                LocalDateTime.now() :
                Instant.ofEpochMilli(endDateMillis).atZone(ZoneId.of("UTC")).toLocalDateTime();
        log.trace(String.format("End traces at => '%s'", endDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        return endDateTime;
    }

    private LocationOutputFormat loadLocationOutputFormatFromParam(final String locationOutputFormat) {
        LocationOutputFormat format;
        try {
            format = LocationOutputFormat.valueOf(locationOutputFormat);
        } catch (final IllegalArgumentException e) {
            log.error("Could not parse ENUM location format output '{}', reverting to default value GEOJSON", locationOutputFormat);
            format = LocationOutputFormat.GEOJSON;
        }
        return format;
    }

    private GPSTrack loadGpsTrackForUserWithOptimizationParameters(final String userId, final List<UnitLocationMeasurement> locations,
                                                                   final boolean isPathOptimizerEnabled,
                                                                   final int optimizationCoefficient,
                                                                   final boolean isGpsJumpFilterEnabled) {
        final List<GPSPoint> points = locations.parallelStream()
                .map(this::map)
                .sorted(GPSPoint::compareTo)
                .collect(Collectors.toList());
        final List<String> trackingDevicesIds = points.stream()
                .map(GPSPoint::getCollectorId)
                .distinct()
                .collect(Collectors.toList());
        log.info("Loaded {} recorded GPS locations from {} tracking device(s) {} for user '{}'", points.size(),
                trackingDevicesIds.size(), trackingDevicesIds.toArray(), userId);

        return GPSTrack.builder()
                .trackedUser(userId)
                .trackingDevicesIds(trackingDevicesIds)
                .track(filterGpsJumps(optimizePath(points, isPathOptimizerEnabled, optimizationCoefficient), isGpsJumpFilterEnabled))
                .build();
    }

    private GPSPoint map(UnitLocationMeasurement unitLocationMeasurement) {
        return GPSPoint.builder()
                .accuracy(unitLocationMeasurement.getAccuracyInMeters())
                .altitude(unitLocationMeasurement.getAltitude())
                .heading(unitLocationMeasurement.getHeading())
                .latitude(unitLocationMeasurement.getLatitude())
                .longitude(unitLocationMeasurement.getLongitude())
                .collectorId(unitLocationMeasurement.getId().getCollectingDeviceId())
                .speed(unitLocationMeasurement.getSpeed())
                .unitId(unitLocationMeasurement.getId().getUnitId())
                .timestamp(unitLocationMeasurement.getId().getTime())
                .build();
    }
}
