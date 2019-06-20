package lu.hitec.dispng.locationexplorer.utils;

import lombok.extern.slf4j.Slf4j;
import lu.hitec.dispng.locationexplorer.basis.Localizable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RamerDouglasPeuckerAlgorithm<E extends Localizable> {

    // private static final int EPSILON_ADJUSTMENTS = 3; // TODO: Use this as configuration parameter for more precise tracks

    private int optimizationCoefficient;

    public RamerDouglasPeuckerAlgorithm(final int optimizationCoefficient) {
        this.optimizationCoefficient = optimizationCoefficient;
    }

    public List<E> getOptimizedPath(final List<E> allPoints) {
        final List<E> optimizedPathPoints = getOptimizedPath(allPoints, calculateAccurateEpsilon(allPoints));
        log.info("Shortened path from {} points down to {} points for optimization using epsilon coefficient '{}'", allPoints.size(), optimizedPathPoints.size(), this.optimizationCoefficient);
        return optimizedPathPoints;
    }

    private List<E> getOptimizedPath(final List<E> allPoints, final double tolerance) {
        log.trace("using tolerance '{}'", tolerance);
        if ((allPoints == null) || (allPoints.size() < 3)) {
            return allPoints;
        }

        final E firstPoint = allPoints.get(0);
        final E lastPoint = allPoints.get(allPoints.size() - 1);
        double maxDistance = 0;
        int farthestPointIndex = 0;

        for (final Localizable currentPoint : allPoints) {
            final double currentPointDistance = GeometryTools.getOrthogonalDistanceBetweenPointAndLine(currentPoint, firstPoint, lastPoint);
            if (currentPointDistance > maxDistance) {
                maxDistance = currentPointDistance;
                farthestPointIndex = allPoints.indexOf(currentPoint);
            }
        }

        log.debug(String.format("Found maxDistance of : %s, for point index : %s", maxDistance, farthestPointIndex));
        final List<E> returnedValue = new ArrayList<>();

        if (maxDistance > tolerance) {
            log.debug("maxDistance still exceed tolerance, about to perform two recursive calls");
            log.debug(String.format("call for first half is between points 0 and %s", farthestPointIndex + 1));
            log.debug(String.format("call for second half is between points %s and %s", farthestPointIndex + 1, allPoints.size()));
            final List<E> firstHalf = new ArrayList<>(allPoints.subList(0, farthestPointIndex + 1));
            final List<E> secondHalf = new ArrayList<>(allPoints.subList(farthestPointIndex, allPoints.size()));
            // recursive calls
            final List<E> firstHalfReturn = getOptimizedPath(firstHalf, tolerance);
            final List<E> secondHalfReturn = getOptimizedPath(secondHalf, tolerance);

            returnedValue.addAll(firstHalfReturn);
            // removing the first element to avoid 2 occurrence of the farthestPoint
            if (!secondHalfReturn.isEmpty()) {
                secondHalfReturn.remove(0);
            }

            returnedValue.addAll(secondHalfReturn);
        } else {
            // if no point is far enough, the evaluated sublist returns only the 2 extreme points
            returnedValue.add(firstPoint);
            returnedValue.add(lastPoint);
        }
        return returnedValue;
    }
    /*
        private <E extends Localizable> double calculateAccurateEpsilon(final E[] allPoints) {
            log.debug("about to calculateAccurateEpsilon");
            if ((allPoints == null) || (allPoints.length == 0)) {
                return 0;
            }
            return calculateAccurateEpsilon(Arrays.asList(allPoints));
        }
    */
    private <E extends Localizable> double calculateAccurateEpsilon(final List<E> allPoints) {
        log.debug("about to calculateAccurateEpsilon");
        if (allPoints.size() == 0) {
            return 0;
        }

        double allDistancesSum = 0;
        Localizable firstPoint = null;
        Localizable secondPoint = null;
        for (final Localizable currentPoint : allPoints) {
            if (secondPoint != null) {
                firstPoint = secondPoint;
            }
            secondPoint = currentPoint;
            final double distanceBetweenPoints = GeometryTools.getDistanceBetweenPoints(firstPoint, secondPoint);
            if ((firstPoint != null) && (secondPoint != null)) {
                log.debug(String.format("about to add distance between Points %s and %s : %s", firstPoint.toString(), secondPoint.toString(),
                        distanceBetweenPoints));
            }
            allDistancesSum += distanceBetweenPoints;
        }
        return allDistancesSum / (allPoints.size() * this.optimizationCoefficient);
    }
}
