package ga.elirey.locationexplorer.utils;

import ga.elirey.locationexplorer.basis.Localizable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ga.elirey.locationexplorer.utils.GeometryTools.distanceInMeters;

@Slf4j
public class GpsJumpsFilterAlgorithm<E extends Localizable> implements AlgorithmExecutor<E>{
    @Override
    public String getName() {
        return "GPS jumps filter";
    }

    @Override
    public List<E> apply(List<E> points) {
        log.info("GPS jumps filtering enabled");
        log.trace("Processing {} points with GPS jumps Filter custom algorithm", points.size());
        final CustomTimer timer = new CustomTimer();

        final List<E> dtoLocationValuesFiltered = new ArrayList<>();
        if (CollectionUtils.isEmpty(points)) {
            return dtoLocationValuesFiltered;
        }
        if (points.size() <= 3) {
            return points;
        }
        final List<E> sortedPoints = points.stream()
                .sorted(Comparator.comparing(E::getTimestamp))
                .collect(Collectors.toList());

        // add first loc anyway
        dtoLocationValuesFiltered.add(sortedPoints.get(0));

        for (int i = 1; i < (sortedPoints.size() - 1); i++) {
            final E dtoLocationValuePrecedent = sortedPoints.get(i - 1);
            final E dtoLocationValue = sortedPoints.get(i);
            final E dtoLocationValueNext = sortedPoints.get(i + 1);
            addIfNoJump(dtoLocationValuesFiltered, dtoLocationValuePrecedent, dtoLocationValue, dtoLocationValueNext);
        }
        // add last loc anyway
        dtoLocationValuesFiltered.add(sortedPoints.get(sortedPoints.size() - 1));
        log.warn("Filtered {} locations as GPS jumps in {} ms, now {} locations remaining for further process",
                points.size() - dtoLocationValuesFiltered.size(), timer.elapsedMsecs(), dtoLocationValuesFiltered.size()); // filterBadAccuracyIfRecentLocationPresent
        return dtoLocationValuesFiltered;
    }

    /**
     * Filter jumps between 3 locations.
     * Sudden change of direction identified by a jump to one direction followed
     * by a return jump near to the first location. To identify it we consider a triangle formed by L1, L2, L3 and
     * filter locations if distances L1->L3 < L1->L2 && L1-L2 < L2 -> L3
     *
     * @param dtoLocationValuesFiltered  the filtered values list to be filled
     * @param dtoLocationValuePrecedent1 the previous recorded value
     * @param dtoLocationValue2          the current value being evaluated too far or not
     * @param dtoLocationValueNext3      the next value
     */
    private void addIfNoJump(final List<E> dtoLocationValuesFiltered,
                                   final E dtoLocationValuePrecedent1, final E dtoLocationValue2,
                                   final E dtoLocationValueNext3) {
        final double distanceL1L2 = distanceInMeters(dtoLocationValuePrecedent1, dtoLocationValue2);
        final double distanceL2L3 = distanceInMeters(dtoLocationValue2, dtoLocationValueNext3);
        final double distanceL1L3 = distanceInMeters(dtoLocationValuePrecedent1, dtoLocationValueNext3);
        if ((distanceL1L2 < distanceL1L3) && (distanceL2L3 < distanceL1L3)) {
            dtoLocationValuesFiltered.add(dtoLocationValue2);
        }
    }
}
