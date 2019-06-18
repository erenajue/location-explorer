
package lu.hitec.dispng.locationexplorer.model;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Metadata;
import io.jenetics.jpx.Speed;
import io.jenetics.jpx.WayPoint;
import lombok.*;
import lu.hitec.dispng.locationexplorer.basis.Localizable;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.Point;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GPSPoint implements Localizable, Comparable<GPSPoint> {

    private String unitId;
    private String collectorId;
    private LocalDateTime timestamp; // Seconds or millis?
    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;
    private double heading;
    private double accuracy;

    @Override
    public GeoJsonObject toGeoJsonObject() {
        final Point point = new Point();
        final LngLatAlt lngLatAlt = new LngLatAlt(this.longitude, this.latitude, this.altitude);
        point.setCoordinates(lngLatAlt);

        final Map<String, Object> properties = new HashMap<>();
        properties.put("unitId", this.unitId);
        properties.put("collectorId", this.collectorId);
        properties.put("timestamp", this.timestamp.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        properties.put("speed", this.speed);
        properties.put("heading", this.heading);
        properties.put("accuracy", this.accuracy);
        final Feature feature = new Feature();
        feature.setGeometry(point);
        feature.setProperties(properties);
        feature.setId(UUID.randomUUID().toString());

        return feature;
    }

    @Override
    public String asGeoJsonString() {
        return null;
    }

    Feature toGeoJsonPoint() {
        return (Feature) toGeoJsonObject();
    }

    LngLatAlt toLngLatAlt() {
        return new LngLatAlt(this.longitude, this.latitude, this.altitude);
    }

    @Override
    public long getTimestampAsMilliSeconds() {
        return this.getTimestamp().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }

    @Override
    public WayPoint toGpxWayPoint() {
        return WayPoint.builder()
                .ele(this.altitude)
                .speed(this.speed, Speed.Unit.METERS_PER_SECOND)
                .src(this.collectorId)
                .time(getTimestampAsMilliSeconds())
                .build(this.latitude, this.longitude);
    }

    @Override
    public double[] as2DDoubleVector() {
        return new double[]{latitude, longitude};
    }

    @Override
    public double[] as3DDoubleVector() {
        return new double[]{latitude, longitude, altitude};
    }

    @Override
    public GPX toGpxObject() {
        return GPX.builder()
                .metadata(Metadata.builder()
                        .author(this.collectorId)
                        .time(getTimestampAsMilliSeconds())
                        .build())
                .addWayPoint(this.toGpxWayPoint())
                .build();
    }

    @Override
    public String asGpxString() {
        return null;
    }

    public int compareTo(GPSPoint otherPoint) {
        return this.timestamp.compareTo(otherPoint.timestamp);
    }
}
