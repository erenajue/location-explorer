package ga.elirey.locationexplorer.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ga.elirey.locationexplorer.basis.Traceable;
import ga.elirey.locationexplorer.exception.InsufficientRequiredPointsException;
import ga.elirey.locationexplorer.utils.GeometryTools;
import io.jenetics.jpx.*;
import lombok.*;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GPSTrack implements Traceable {

    private String trackedUser;

    // TODO: Add all  the device being tracked using data from points (stream + distinct)
    @Singular("device")
    private List<String> trackingDevicesIds;

    @Singular("point")
    private List<GPSPoint> track;

    private Optional<GPSPoint> getStartPoint() {
        return track.stream()
                .min(GPSPoint::compareTo);
    }

    private Optional<GPSPoint> getEndPoint() {
        return track.stream()
                .max(GPSPoint::compareTo);
    }

    @Override
    public OptionalLong getStartDate() {
        return track.stream()
                .mapToLong(GPSPoint::getTimestampAsMilliSeconds)
                .min();
    }

    @Override
    public OptionalLong getEndDate() {
        return track.stream()
                .mapToLong(GPSPoint::getTimestampAsMilliSeconds)
                .max();
    }

    private static final int MINUTES_AS_SECONDS = 60;
    private static final int HOURS_AS_MINUTES = 60;
    private static final int DAYS_AS_HOURS = 24;

    @Override
    public String getDuration() {

        // TODO: Improve
        Optional<GPSPoint> startPoint = this.getStartPoint();
        Optional<GPSPoint> endPoint = this.getEndPoint();

        Duration d = Duration.ZERO;

        if (startPoint.isPresent() && endPoint.isPresent()) {
            d = Duration.between(startPoint.get().getTimestamp(), endPoint.get().getTimestamp());
        }

        final long durationInSeconds = d.getSeconds();

        if (durationInSeconds < MINUTES_AS_SECONDS) { // less than a minute => seconds
            return String.format("%d seconds", durationInSeconds);
        } else if (durationInSeconds < HOURS_AS_MINUTES * MINUTES_AS_SECONDS) { // less than an hour => minutes
            return String.format("%d minutes", d.toMinutes());
        } else if (durationInSeconds < DAYS_AS_HOURS * HOURS_AS_MINUTES * MINUTES_AS_SECONDS) { // less than a day => hours
            return String.format("%d hours", d.toHours());
        } else { // more than a day => days
            return String.format("%d days", d.toDays());
        }
    }

    @Override
    public OptionalDouble getAverageSpeed() {
        return track.stream()
                .mapToDouble(GPSPoint::getSpeed)
                .average();
    }

    @Override
    public OptionalDouble getAverageHeading() {
        return track.stream()
                .mapToDouble(GPSPoint::getHeading)
                .average();
    }

    @Override
    public OptionalDouble getAverageAccuracy() {
        return track.stream()
                .mapToDouble(GPSPoint::getAccuracy)
                .average();
    }

    @Override
    public GeoJsonObject toGeoJsonObject() {

        final FeatureCollection features = new FeatureCollection();
        features.add(asLineStringFeature());
        return features;
    }

    @Override
    public String asGeoJsonString() throws JsonProcessingException {
        final FeatureCollection geoJsonTrack = (FeatureCollection) this.toGeoJsonObject();
        return new ObjectMapper().writeValueAsString(geoJsonTrack);
    }

    public String asGeoJsonString(final boolean isPointsIncluded) throws JsonProcessingException {
        final FeatureCollection geoJsonTrack = (FeatureCollection) this.toGeoJsonObject();
        if (isPointsIncluded) {
            geoJsonTrack.addAll(asPointFeature());
        }
        return new ObjectMapper().writeValueAsString(geoJsonTrack);
    }

    private List<Feature> asPointFeature() {
        // generate 2 features: 1 set of points and 1 linestring
        return track.stream()
                .map(GPSPoint::toGeoJsonPoint)
                .collect(Collectors.toList());
    }

    private Feature asLineStringFeature() {

        // TODO: Separate in distinct method then implement call to fill feature collection
        final LineString ls = new LineString();
        ls.setCoordinates(track.stream()
                .map(GPSPoint::toLngLatAlt)
                .collect(Collectors.toList()));

        final Feature lineFeature = new Feature();
        lineFeature.setGeometry(ls);
        final Map<String, Object> properties = new HashMap<>();

        // TODO: Regroup in one single stat-oriented object (use of @See SummaryStatistics for (stream) double object)
        // TODO: Move these fields in other class
        double[] defaultPoint = {0.0, 0.0, 0.0};
        if (track.size() == 1) {
            GPSPoint lonePoint = track.get(0);
            defaultPoint = lonePoint.as3DDoubleVector();
        }
        properties.put("startPoint", this.getStartPoint().map(GPSPoint::as3DDoubleVector).orElse(defaultPoint));
        properties.put("endPoint", this.getEndPoint().map(GPSPoint::as3DDoubleVector).orElse(defaultPoint));
        properties.put("startDate", new Date(this.getStartDate().orElse(0)).toString());
        properties.put("endDate", new Date(this.getEndDate().orElse(0)).toString());
        properties.put("tracksSize", track.size());
        properties.put("trackedUser", this.getTrackedUser());
        properties.put("trackedDevices", this.getTrackingDevicesIds().toArray());
        properties.put("averageSpeed", this.getAverageSpeed().orElse(0.0));
        properties.put("averageHeading", this.getAverageHeading().orElse(0.0));
        properties.put("averageAccuracyInMeters", this.getAverageAccuracy().orElse(10.0));
        properties.put("duration", this.getDuration());
        properties.put("travelledDistanceInMeters", this.getTravelledDistance());

        lineFeature.setProperties(properties);
        lineFeature.setId(UUID.randomUUID().toString());

        return lineFeature;
    }

    @Override
    public GPX toGpxObject() {

        // TODO: Add metadata to define the content of the GPX file

        // TODO: Change this when we will be able to organize track in segments (based on dates for instance)
        final TrackSegment uniqueSegment = TrackSegment.builder()
                .points(track.stream()
                        .map(GPSPoint::toGpxWayPoint)
                        .collect(Collectors.toList()))
                .build();

        // TODO: Change this when we will be able to manage more than one single track (based on dates for instance)
        final Track uniqueTrack = Track.builder()
                .addSegment(uniqueSegment)
                .build();

        // TODO: CHeck if this can be added as XML tags
        final String description = String.format("%n startDate : %s", new Date(this.getStartDate().orElse(0)).toString())
                .concat(String.format("%n endDate : %s", new Date(this.getEndDate().orElse(0)).toString()))
                .concat(String.format("%n trackSize : %d", this.size()))
                .concat(String.format("%n trackedDevices : %s", this.getTrackingDevicesIds().toArray()))
                .concat(String.format("%n averageSpeed : %s", this.getAverageSpeed().orElse(0.0)))
                .concat(String.format("%n averageHeading : %s", this.getAverageHeading().orElse(0.0)))
                .concat(String.format("%n averageAccuracyInMeters : %s", this.getAverageAccuracy().orElse(10.0)))
                .concat(String.format("%n duration : %s", this.getDuration()))
                .concat(String.format("%n travelledDistanceInMeters : %s", this.getTravelledDistance()));

        return GPX.builder()
                .creator(this.trackedUser)
                .metadata(Metadata.builder()
                        .author(Person.of(this.trackedUser))
                        .desc(description)
                        .name(String.format("GPX traces of %s", this.trackedUser))
                        .time(Instant.now())
                        .build())
                .addTrack(uniqueTrack)
                .addWayPoint(this.getStartPoint()
                        .map(GPSPoint::toGpxWayPoint)
                        .orElseThrow(InsufficientRequiredPointsException::new)) // get start point of sole point if there is any
                .addWayPoint(this.getEndPoint()
                        .map(GPSPoint::toGpxWayPoint)
                        .orElseThrow(InsufficientRequiredPointsException::new)) // get end point of sole point if there is any
                .build();
    }

    @Override
    public String asGpxString() throws IOException {
        ByteArrayOutputStream gpxBaos = new ByteArrayOutputStream();
        final GPX lineSegment = this.toGpxObject();
        GPX.write(lineSegment, gpxBaos);
        return gpxBaos.toString();
    }

    private double getTravelledDistance() {
        double distance = 0.0;
        for (int i = 0; i < track.size() - 1; i++) {
            distance += GeometryTools.getDistanceBetweenPointsInMeters(track.get(i), track.get(i + 1));
        }
        return Math.ceil(distance);
    }

    public int size() {
        return track.size();
    }
}

