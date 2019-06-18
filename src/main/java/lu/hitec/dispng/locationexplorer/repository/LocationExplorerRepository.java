package lu.hitec.dispng.locationexplorer.repository;

import lu.hitec.dispng.locationexplorer.entity.UnitLocationMeasurement;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationExplorerRepository extends CrudRepository<UnitLocationMeasurement, UnitLocationMeasurement.Id> {

    // List<UnitLocationMeasurement> findByIdUnitIdAndIdMissionId(String unitId, String missionId);

    List<UnitLocationMeasurement> findByIdTimeAfterAndIdTimeBeforeAndIdUnitIdAndIdMissionId(LocalDateTime startDate,
                                                                                            LocalDateTime endDate, String unitId, String missionId);

}
