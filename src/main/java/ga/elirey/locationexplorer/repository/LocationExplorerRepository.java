package ga.elirey.locationexplorer.repository;

import ga.elirey.locationexplorer.entity.UnitLocationMeasurement;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationExplorerRepository extends CrudRepository<UnitLocationMeasurement, UnitLocationMeasurement.Id> {

    List<UnitLocationMeasurement> findByIdTimeAfterAndIdTimeBeforeAndIdUnitIdAndIdMissionId(LocalDateTime startDate,
                                                                                            LocalDateTime endDate, String unitId, String missionId);
}
