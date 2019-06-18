package lu.hitec.dispng.locationexplorer.utils;

import lombok.extern.slf4j.Slf4j;
import lu.hitec.dispng.locationexplorer.basis.Localizable;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class GpsJumpsFilterAlgorithm<E extends Localizable> {

    public List<E> filterJumps(final List<E> locValues) {
        final CustomTimer timer = new CustomTimer();

        final List<E> dtoLocationValuesFiltered = new ArrayList<>();
        if (CollectionUtils.isEmpty(locValues)) {
            return dtoLocationValuesFiltered;
        }
        if (locValues.size() <= 3) {
            return locValues;
        }
        final List<E> sortedLocValues = locValues.stream()
                .sorted(Comparator.comparing(E::getTimestamp))
                .collect(Collectors.toList());

        // add first loc anyway
        dtoLocationValuesFiltered.add(sortedLocValues.get(0));

        for (int i = 1; i < (sortedLocValues.size() - 1); i++) {
            final E dtoLocationValuePrecedent = sortedLocValues.get(i - 1);
            final E dtoLocationValue = sortedLocValues.get(i);
            final E dtoLocationValueNext = sortedLocValues.get(i + 1);
            addIfNoJump(dtoLocationValuesFiltered, dtoLocationValuePrecedent, dtoLocationValue, dtoLocationValueNext);
        }
        // add last loc anyway
        dtoLocationValuesFiltered.add(sortedLocValues.get(sortedLocValues.size() - 1));
        log.warn("Filtered {} locations as GPS jumps in {} ms, now {} locations remaining for further process",
                timer.elapsedMsecs(), locValues.size() - dtoLocationValuesFiltered.size(), dtoLocationValuesFiltered.size()); // filterBadAccuracyIfRecentLocationPresent
        return dtoLocationValuesFiltered;
    }

    /**
     * TODO: Move this part in geometry tools class
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

    /**
     * TODO: Move this part in geometry tools class
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     * <p>
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     *
     * @return Distance in Meters
     */
    private static double distanceInMeters(final double lat1, final double lat2, final double lon1, final double lon2, final Double el1,
                                           final Double el2) {

        final int R = 6371; // Radius of the earth in kilometers

        final double latDistance = Math.toRadians(lat2 - lat1);
        final double lonDistance = Math.toRadians(lon2 - lon1);
        final double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2))
                + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2));
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        final double distance = R * c * 1000; // convert to meters

        if ((el1 != null) && (el2 != null)) {
            final double height = el1 - el2;

            final double distanceWithHeight = Math.pow(distance, 2) + Math.pow(height, 2);
            return Math.sqrt(distanceWithHeight);
        }

        return distance;
    }

    // TODO: Move to geometry tools?
    private double distanceInMeters(final E loc1, final E loc2) {
        return distanceInMeters(loc1.getLatitude(), loc2.getLatitude(), loc1.getLongitude(), loc2.getLongitude(), loc1.getAltitude(), loc2.getAltitude());
    }


}
