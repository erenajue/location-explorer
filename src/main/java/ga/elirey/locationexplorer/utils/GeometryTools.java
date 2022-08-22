package ga.elirey.locationexplorer.utils;

import ga.elirey.locationexplorer.basis.Localizable;

public final class GeometryTools {

    private static final double DELTA = 0.0001;

    private static final String KILOMETERS = "K";

    private static final String MILES = "M";

    private GeometryTools() {
    }

    /**
     * to check that a point is on one side of a line, we check if it is on the same side than another point we consider to be on the good
     * side
     *
     * @param pointToBeTested tested point
     * @param comparisonPoint point of comparison
     * @param firstPointOfLine first point of the straight line assumed to join both the tested dans compared point
     * @param secondPointOfLine second point of the straight line assumed to join both the tested dans compared point
     * @return true if points are on the same side of line
     */
    public static boolean arePointsOnTheSameSideOfLine(final Localizable pointToBeTested, final Localizable comparisonPoint,
                                                       final Localizable firstPointOfLine, final Localizable secondPointOfLine) {
        final boolean pointIsAbove = isPointAboveLine(pointToBeTested, firstPointOfLine, secondPointOfLine);
        final boolean comparisonPointIsAbove = isPointAboveLine(comparisonPoint, firstPointOfLine, secondPointOfLine);

        return pointIsAbove == comparisonPointIsAbove;
    }

    public static boolean equals(final Localizable p1, final Localizable p2) {
        return equals(p1.getLatitude(), p2.getLatitude()) && equals(p1.getLongitude(), p2.getLongitude());
    }

    /**
     * gets the longitude of the point of crossing between a segment and an horizontal line Segment is determined by its 2 extremities Line is
     * determined by its latitude
     * relevant only if rayCastingTestForSegment is true for the same values
     *
     * @param firstLocation
     * @param secondLocation
     * @param referenceLatitude
     * @return the longitude of the crossing point if exists, null otherwise
     */
    public static Double findCrossingLongitude(final Localizable firstLocation, final Localizable secondLocation,
                                               final double referenceLatitude) {
        if (rayCastingTestForSegment(firstLocation, secondLocation, referenceLatitude)) {
            if (equals(firstLocation.getLatitude(), secondLocation.getLatitude())) {
                return firstLocation.getLongitude();
            }

            final double latDif = firstLocation.getLatitude() - secondLocation.getLatitude();
            final double lngDif = firstLocation.getLongitude() - secondLocation.getLongitude();
            final double latOffset = firstLocation.getLatitude() - referenceLatitude;

            final double offset = latOffset / latDif;

            return firstLocation.getLongitude() - (lngDif * offset);
        }
        return null;
    }

    public static double getDistanceBetweenPoints(final Localizable point1, final Localizable point2) {
        if ((point1 == null) || (point2 == null)) {
            return 0;
        }

        final double latDif = point1.getLatitude() - point2.getLatitude();
        final double lngDif = point1.getLongitude() - point2.getLongitude();
        return Math.sqrt(Math.pow(latDif, 2) + Math.pow(lngDif, 2));
    }

    public static double getDistanceBetweenPointsInMeters(final Localizable p1, final Localizable p2) {
        return getDistanceBetweenPoints(p1, p2, KILOMETERS) * 1000;
    }

    public static double getDistanceBetweenPointsInMiles(final Localizable p1, final Localizable p2) {
        return getDistanceBetweenPoints(p1, p2, MILES);
    }

    /**
     * please note this does not return a distance in meters (or miles) but instead a Lat/lng like distance, which can not reliably be
     * converted in a accurate real spherical distance
     *
     * @param pointToBeTested
     * @param firstPointOfLine
     * @param secondPointOfLine
     * @return orthogonal distance between point and line
     */
    public static double getOrthogonalDistanceBetweenPointAndLine(final Localizable pointToBeTested,
                                                                  final Localizable firstPointOfLine, final Localizable secondPointOfLine) {
        double area;
        double bottom;
        double height;

        area = Math
                .abs((((firstPointOfLine.getLatitude() * secondPointOfLine.getLongitude()) + (secondPointOfLine.getLatitude() * pointToBeTested.getLongitude()) + (pointToBeTested
                        .getLatitude() * firstPointOfLine.getLongitude()))
                        - (secondPointOfLine.getLatitude() * firstPointOfLine.getLongitude())
                        - (pointToBeTested.getLatitude() * secondPointOfLine.getLongitude()) - (firstPointOfLine.getLatitude() * pointToBeTested.getLongitude())) * 0.5);

        bottom = Math.sqrt(Math.pow(firstPointOfLine.getLatitude() - secondPointOfLine.getLatitude(), 2)
                + Math.pow(firstPointOfLine.getLongitude() - secondPointOfLine.getLongitude(), 2));

        height = (area / bottom) * 2.0;

        return height;
    }

    /**
     * 2D geometry tests if a point is "above" or "below" a line defined by 2 other points above means more on the North and/or on the West of
     * the line this is an arbitrary statement
     * that has only a value for comparison Lng and Lat are considered as (x, y) values of a point in a pseudo-euclidian plane we determine a
     * function f(x) = Ax + B and return true
     * if y' >= Ax' + B, for the tested point (x', y')
     *
     * @param pointToBeTested
     * @param firstPointOfLine
     * @param secondPointOfLine
     * @return true if the point is above the line
     */
    public static boolean isPointAboveLine(final Localizable pointToBeTested, final Localizable firstPointOfLine,
                                           final Localizable secondPointOfLine) {
        Localizable leftPoint;
        Localizable rightPoint;

        if (firstPointOfLine.getLongitude() < secondPointOfLine.getLongitude()) {
            leftPoint = firstPointOfLine;
            rightPoint = secondPointOfLine;
        } else if (firstPointOfLine.getLongitude() > secondPointOfLine.getLongitude()) {
            rightPoint = firstPointOfLine;
            leftPoint = secondPointOfLine;
        } else if (firstPointOfLine.getLatitude() < secondPointOfLine.getLatitude()) {
            leftPoint = firstPointOfLine;
            rightPoint = secondPointOfLine;
        } else {
            rightPoint = firstPointOfLine;
            leftPoint = secondPointOfLine;
        }

        final double xDiff = rightPoint.getLongitude() - leftPoint.getLongitude();
        final double yDiff = rightPoint.getLatitude() - leftPoint.getLatitude();
        final double aFactor = yDiff / xDiff;

        final double lngDist = pointToBeTested.getLongitude() - leftPoint.getLongitude();
        final double latDist = pointToBeTested.getLatitude() - leftPoint.getLatitude();
        return latDist > (lngDist * aFactor);
    }

    public static boolean pointsOnSameLatitude(final Localizable firstLocation, final Localizable secondLocation) {
        return (firstLocation != null) && (secondLocation != null) && equals(firstLocation.getLatitude(), secondLocation.getLatitude());
    }

    /**
     * tests if a segment has a point that crosses the horizontal line determined by its latitude
     *
     * @param firstLocation the first point of the segment
     * @param secondLocation the second point of the segment
     * @param referenceLatitude the latitude of the line
     */
    public static boolean rayCastingTestForSegment(final Localizable firstLocation, final Localizable secondLocation,
                                                   final double referenceLatitude) {
        if ((firstLocation == null) || (secondLocation == null)) {
            return false;
        }
        // true if one of the points is exactly at this latitude
        if (equals(firstLocation.getLatitude(), referenceLatitude) || equals(secondLocation.getLatitude(), referenceLatitude)) {
            return true;
        }

        // true if one of the points above, AND the other below
        if ((firstLocation.getLatitude() > referenceLatitude) && (secondLocation.getLatitude() < referenceLatitude)) {
            return true;
        }
        return (firstLocation.getLatitude() < referenceLatitude) && (secondLocation.getLatitude() > referenceLatitude);

        // false otherwise (both points above, or both below)
    }

    /**
     * tests if a segment has a point that crosses the horizontal semi-line determined by the tested point latitude true if the segment as a
     * point at the same latitude than the
     * tested point , and that this point longitude is greater than the tested point longitude
     *
     * To deal with a bug if a vertex is the crossing point, true is returned only if the other point is below the tested point
     *
     * @param firstLocation the first point of the segment
     * @param secondLocation the second point of the segment
     * @param testedPoint the point to test
     */
    public static boolean uniDirectionalRayCastingTestForSegment(final Localizable firstLocation, final Localizable secondLocation,
                                                                 final Localizable testedPoint) {
        if ((firstLocation == null) || (secondLocation == null)) {
            return false;
        }

        // dealing with the unlucky case of the 3 points aligned horizontaly
        if (pointsOnSameLatitude(firstLocation, testedPoint) || pointsOnSameLatitude(secondLocation, testedPoint)) {
            return (firstLocation.getLatitude() < testedPoint.getLatitude()) || (secondLocation.getLatitude() < testedPoint.getLatitude());
        }

        final Double crossingLongitude = findCrossingLongitude(firstLocation, secondLocation, testedPoint.getLatitude());
        return (crossingLongitude != null) && ((crossingLongitude > testedPoint.getLongitude()) || equals(crossingLongitude, testedPoint.getLongitude()));
    }

    /**
     * Converts the value from Degrees to radians
     *
     * @param deg the angle's degrees value
     * @return value in radians
     */
    private static double deg2rad(final double deg) {
        return (deg * Math.PI) / 180.0;
    }

    private static boolean equals(final double d1, final double d2) {
        return d1 == d2 || Math.abs(d1 - d2) < DELTA;

    }

    private static double getDistanceBetweenPoints(final Localizable p1, final Localizable p2, final String unit) {
        final double theta = p1.getLongitude() - p2.getLongitude();
        final double dist1 = Math.sin(deg2rad(p1.getLatitude())) * Math.sin(deg2rad(p2.getLatitude()));
        final double dist2 = Math.cos(deg2rad(p1.getLatitude())) * Math.cos(deg2rad(p2.getLatitude())) * Math.cos(deg2rad(theta));
        double dist = dist1 + dist2;
        dist = Math.acos(Math.min(dist, 1.0));
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if ("K".equals(unit)) {
            dist = dist * 1.609344;
        } else if ("M".equals(unit)) {
            dist = dist * 0.8684;
        }
        return dist;
    }

    /**
     * Method used to convert the value form radians to degrees
     *
     * @param rad the angle radian value
     * @return value in degrees
     */
    private static double rad2deg(final double rad) {
        return (rad * 180.0) / Math.PI;
    }

}
