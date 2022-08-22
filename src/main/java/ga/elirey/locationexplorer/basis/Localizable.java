package ga.elirey.locationexplorer.basis;

import io.jenetics.jpx.WayPoint;

import java.time.LocalDateTime;

public interface Localizable extends LocationFormat {

    /**
     * latitude from GPS-equipped device
     *
     * @return the latitude
     */
    double getLatitude();

    /**
     * set latitude in GPS-type device
     *
     * @param latitude to set
     */
    void setLatitude(double latitude);

    /**
     * longitude from GPS-equipped device
     *
     * @return the longitude
     */
    double getLongitude();

    /**
     * set longitude in GPS-type device
     *
     * @param longitude to set
     */
    void setLongitude(double longitude);

    /**
     * altitude from GPS-equipped device
     *
     * @return the altitude
     */
    double getAltitude();

    /**
     * set altitude in GPS-type device
     *
     * @param altitude to set
     */
    void setAltitude(double altitude);

    /**
     * timestamp from a GPS device  for the location
     *
     * @return the timestamp
     */
    LocalDateTime getTimestamp();

    /**
     * timestamp in milliseconds
     *
     * @return the timestamp in milliseconds as long
     */
    long getTimestampAsMilliSeconds();

    /**
     * returns a gpx way point for a single location
     *
     * @return a way point
     */
    WayPoint toGpxWayPoint();

    /**
     * the localizable accuracy in meter
     *
     * @return accuracy
     */
    double getAccuracy();

    /**
     * sets the accuracy
     *
     * @param accuracy to set
     */
    void setAccuracy(double accuracy);

    /**
     * Technical method to turn a point to a 2-D array of latitude and longitude
     *
     * @return a double array
     */
    double[] as2DDoubleVector();

    /**
     * Technical method to turn a point to a 3-D array of latitude, longitude and altitude
     *
     * @return a double array
     */
    double[] as3DDoubleVector();
}
