package ga.elirey.locationexplorer.service;

import ga.elirey.locationexplorer.data.FilterOptions;
import ga.elirey.locationexplorer.entity.UnitLocationMeasurement;
import ga.elirey.locationexplorer.format.LocationOutputFormat;
import ga.elirey.locationexplorer.model.GPSPoint;
import ga.elirey.locationexplorer.model.GPSTrack;
import ga.elirey.locationexplorer.utils.GpsJumpsFilterAlgorithm;
import ga.elirey.locationexplorer.utils.RamerDouglasPeuckerAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationExplorerService {

    private final LocationPollerService pollerService;

    /**
     * Convert a database entry to a geojson or a gpx entry (other location data format will be added progressively)
     *
     * @return a string version of the produced geojson
     */
    @Transactional(readOnly = true)
    public String convert(final String userId, final String missionId, final String outputFormat,
                          final Long startDateMillis, final Long endDateMillis,
                          final FilterOptions options) throws Exception {

        final List<UnitLocationMeasurement> locations = pollerService.getLocations(userId, missionId, startDateMillis, endDateMillis);

        final GPSTrack gpsTrack = loadGpsTrackForUserWithOptimizationParameters(userId, locations, options);

        return processAndFormat(outputFormat, gpsTrack, options);
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

    private String processAndFormat(final String locationOutputFormat, final GPSTrack gpsTrack, FilterOptions options) throws IOException {
        LocationOutputFormat format;
        try {
            format = LocationOutputFormat.valueOf(locationOutputFormat);
        } catch (final IllegalArgumentException e) {
            log.error("Could not parse ENUM location format output '{}', reverting to default value GEOJSON", locationOutputFormat);
            format = LocationOutputFormat.GEOJSON;
        }
        return format.process(gpsTrack, options);
    }

    private GPSTrack loadGpsTrackForUserWithOptimizationParameters(final String userId,
                                                                   final List<UnitLocationMeasurement> locations,
                                                                   final FilterOptions filterOptions) {
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
                .track(filterGpsJumps(optimizePath(points, filterOptions.isPathOptimizerEnabled(),
                        filterOptions.getOptimizationCoefficient()), filterOptions.isGpsJumpFilterEnabled()))
                .build();
    }

    private GPSPoint map(UnitLocationMeasurement unitLocationMeasurement) {
        return GPSPoint.builder()
                .accuracy(unitLocationMeasurement.getAccuracyInMeters())
                .altitude(unitLocationMeasurement.getAltitude())
                .heading(unitLocationMeasurement.getHeading())
                .latitude(unitLocationMeasurement.getLatitude())
                .longitude(unitLocationMeasurement.getLongitude())
                .collectorId(unitLocationMeasurement.getId().getDeviceId())
                .speed(unitLocationMeasurement.getSpeed())
                .unitId(unitLocationMeasurement.getId().getUnitId())
                .timestamp(unitLocationMeasurement.getId().getTime())
                .build();
    }
}
