package lu.hitec.dispng.locationexplorer.service;

import lombok.extern.slf4j.Slf4j;
import lu.hitec.dispng.locationexplorer.constant.LocationOutputFormat;
import lu.hitec.dispng.locationexplorer.entity.UnitLocationMeasurement;
import lu.hitec.dispng.locationexplorer.model.GPSPoint;
import lu.hitec.dispng.locationexplorer.model.GPSTrack;
import lu.hitec.dispng.locationexplorer.repository.LocationExplorerRepository;
import lu.hitec.dispng.locationexplorer.utils.CustomTimer;
import lu.hitec.dispng.locationexplorer.utils.GpsJumpsFilterAlgorithm;
import lu.hitec.dispng.locationexplorer.utils.RamerDouglasPeuckerAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LocationExplorerService {

    @Autowired
    LocationExplorerRepository repository;

    /**
     * Convert a database entry to a geojson or a gpx entry (other location data format will be added progressively)
     *
     * @return a string version of the produced geojson
     */
    @Transactional(readOnly = true)
    public String convert(final String userId, final String missionId, final String outputFormat, final Long startDateMillis, final Long endDateMillis, final boolean isPathOptimizerEnabled, final boolean isGpsJumpFilterEnabled) throws Exception {

        final LocalDateTime startDateTime = setStartTimeFromParam(startDateMillis);
        final LocalDateTime endDateTime = setStopTimeFromParam(endDateMillis);
        log.info("Request all locations from => {} to => {} for user =>  '{}' in mission => '{}'", startDateTime, endDateTime, userId, missionId);

        final List<UnitLocationMeasurement> locations = repository.findByIdTimeAfterAndIdTimeBeforeAndIdUnitIdAndIdMissionId(startDateTime, endDateTime, userId, missionId);

        final GPSTrack gpsTrack = loadGpsTrackForUserWithOptimizationParameters(userId, locations, isPathOptimizerEnabled, isGpsJumpFilterEnabled);
        final LocationOutputFormat format = loadLocationOutputFormatFromParam(outputFormat);
        switch (format) {
            default:
            case GEOJSON:
                final CustomTimer timerGeoJsonOperation = new CustomTimer();
                log.info("Start GEOJSON transformation...");
                final String geojson = gpsTrack.asGeoJsonString();
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

    private List<GPSPoint> optimizePath(final List<GPSPoint> points, final Boolean isPathOptimizerEnabled) {
        log.info("Path optimization {}!", isPathOptimizerEnabled ? "enabled" : "disabled");
        if (isPathOptimizerEnabled) {
            log.trace("Processing {} points with Ramer-Douglas-Peucker algorithm", points.size());
            final RamerDouglasPeuckerAlgorithm<GPSPoint> rdp = new RamerDouglasPeuckerAlgorithm<>();
            return rdp.getOptimizedPath(points);
        } else return points; // do nothing
    }

    private List<GPSPoint> filterGpsJumps(final List<GPSPoint> points, final Boolean isGpsJumpFilterEnabled) {
        log.info("GPS jumps filtering {}!", isGpsJumpFilterEnabled ? "enabled" : "disabled");
        if (isGpsJumpFilterEnabled) {
            log.trace("Processing {} points with GPS jumps Filter custom algorithm", points.size());
            GpsJumpsFilterAlgorithm<GPSPoint> gjf = new GpsJumpsFilterAlgorithm<>();
            return gjf.filterJumps(points);
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
        } catch (Exception e) {
            log.error("Could not parse ENUM location format output '{}', reverting to default value GEOJSON", locationOutputFormat);
            format = LocationOutputFormat.GEOJSON;
        }
        return format;
    }

    private GPSTrack loadGpsTrackForUserWithOptimizationParameters(final String userId, final List<UnitLocationMeasurement> locations, final boolean isPathOptimizerEnabled, final boolean isGpsJumpFilterEnabled) {
        final List<GPSPoint> points = locations.parallelStream()
                .map(this::map)
                .sorted(GPSPoint::compareTo)
                .collect(Collectors.toList());
        final List<String> trackingDevicesIds = points.stream()
                .map(GPSPoint::getCollectorId)
                .distinct()
                .collect(Collectors.toList());
        log.info("Loaded {} recorded GPS locations from {} tracking device(s) {} for user '{}'", points.size(), trackingDevicesIds.size(), trackingDevicesIds.toArray(), userId);

        return GPSTrack.builder()
                .trackedUser(userId)
                .trackingDevicesIds(trackingDevicesIds)
                .track(filterGpsJumps(optimizePath(points, isPathOptimizerEnabled), isGpsJumpFilterEnabled))
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
