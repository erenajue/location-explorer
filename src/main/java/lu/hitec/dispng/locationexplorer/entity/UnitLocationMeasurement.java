package lu.hitec.dispng.locationexplorer.entity;

import lombok.*;
import lu.hitec.dispng.locationexplorer.constant.LocationStatus;
import lu.hitec.dispng.locationexplorer.constant.UnitType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@EqualsAndHashCode
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

    @Column(name = "statical")
    private Boolean statical;
/*
    @Column(name = "reported_time", columnDefinition = "timestamp", updatable = false)
    private LocalDateTime reportedTime;
*/
    @PrePersist
    void init() {
        if(this.statical == null) {
            this.statical = Boolean.FALSE;
        }
 //       this.reportedTime = LocalDateTime.now();
    }

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

        @Column(name = "unit_type", nullable = false, updatable = false)
        @Enumerated(EnumType.STRING)
        private UnitType relatedUnitType;

        @Column(name = "middleware", nullable = false)
        private String middlewareId;

        @Column(name = "mission_id", nullable = false, updatable = false)
        private String missionId;

        @Column(name = "time", columnDefinition = "timestamp", nullable = false, updatable = false)
        private LocalDateTime time;

        @Column(name = "collecting_device_id", nullable = false, updatable = false)
        private String collectingDeviceId;
    }
}
