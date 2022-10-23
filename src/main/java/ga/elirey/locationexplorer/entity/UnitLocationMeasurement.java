package ga.elirey.locationexplorer.entity;

import lombok.*;
import ga.elirey.locationexplorer.format.LocationStatus;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "unit_location_measurement")
public class UnitLocationMeasurement implements Serializable {

    @EmbeddedId
    private Id id;

    @Column(name = "lat", nullable = false)
    private double latitude;

    @Column(name = "lng", nullable = false)
    private double longitude;

    @Column(name = "alt")
    private Double altitude;

    @Column(name = "accuracy")
    private Double accuracyInMeters;

    @Column(name = "geofencing_status")
    @Enumerated(EnumType.STRING)
    private LocationStatus geofencingStatus;

    @Column(name = "hdg")
    private Double heading;

    @Column(name = "speed")
    private Double speed;

    @Embeddable
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Id implements Serializable {

        private static final long serialVersionUID = -497336290130314451L;

        @Column(name = "unit_id", nullable = false, updatable = false)
        private String unitId;

        @Column(name = "context_id", nullable = false, updatable = false)
        private String contextId;

        @Column(name = "time", columnDefinition = "timestamp", nullable = false, updatable = false)
        private LocalDateTime time;

        @Column(name = "device_id", nullable = false, updatable = false)
        private String deviceId;


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Id id = (Id) o;
            return unitId.equals(id.unitId)
                    && contextId.equals(id.contextId)
                    && time.equals(id.time)
                    && deviceId.equals(id.deviceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(unitId, contextId, time, deviceId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UnitLocationMeasurement that = (UnitLocationMeasurement) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
