package ga.elirey.locationexplorer.utils;

import ga.elirey.locationexplorer.basis.Localizable;
import lombok.experimental.UtilityClass;
@UtilityClass
public final class GeometryTools {

    private static final double LOCALIZABLE_DELTA_CONSIDERATION = 0.0001;

    private static final String KILOMETERS_UNIT = "K";


    public static boolean equals(final Localizable p1, final Localizable p2) {
        return equals(p1.getLatitude(), p2.getLatitude()) && equals(p1.getLongitude(), p2.getLongitude());
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
        return getDistanceBetweenPoints(p1, p2, KILOMETERS_UNIT) * 1000;
    }

    /**
     * please note this does not return a distance in meters (or miles) but instead a Lat/lng like distance, which can not reliably be
     * converted in an accurate real spherical distance
     *
     * @param pointToBeTested point to evaluate
     * @param firstPointOfLine line's entry point
     * @param secondPointOfLine line's exit point
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
     * Converts the value from Degrees to radians
     *
     * @param deg the angle's degrees value
     * @return value in radians
     */
    public static double deg2rad(final double deg) {
        return (deg * Math.PI) / 180.0;
    }

    private static boolean equals(final double d1, final double d2) {
        return d1 == d2 || Math.abs(d1 - d2) < LOCALIZABLE_DELTA_CONSIDERATION;

    }

    public static double getDistanceBetweenPoints(final Localizable p1, final Localizable p2, final String unit) {
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
    public static double rad2deg(final double rad) {
        return (rad * 180.0) / Math.PI;
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     * <p>
     * lat1, lon1 Start point lat2, lon2 Localizable end point el1 Start altitude in meters
     * el2 Localizable end altitude in meters
     *
     * @return Distance in Meters
     */
    private static double distanceInMeters(final double lat1, final double lat2, final double lon1, final double lon2,
                                           final Double el1, final Double el2) {

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

    /**
     * Calculate distance in meters between 2 points
     * @param loc1 start point
     * @param loc2 finish point
     * @return a distance in meters
     */
    public static double distanceInMeters(final Localizable loc1, final Localizable loc2) {
        return distanceInMeters(loc1.getLatitude(), loc2.getLatitude(), loc1.getLongitude(), loc2.getLongitude(),
                loc1.getAltitude(), loc2.getAltitude());
    }
}
