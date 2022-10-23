package ga.elirey.locationexplorer.service;

import ga.elirey.locationexplorer.entity.UnitLocationMeasurement;
import ga.elirey.locationexplorer.repository.LocationExplorerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class LocationPollerService {

    private final LocationExplorerRepository repository;

    public List<UnitLocationMeasurement> getLocations(final String userId, final String missionId,
                                                      final long startDateMillis, final long endDateMillis) {
        return repository.findByIdTimeAfterAndIdTimeBeforeAndIdUnitIdAndIdMissionId(setStartTimeFromParam(startDateMillis),
                setStopTimeFromParam(endDateMillis), userId, missionId);
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
}
