package lu.hitec.dispng.locationexplorer.basis;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.jenetics.jpx.GPX;
import org.geojson.GeoJsonObject;

import java.io.IOException;

public interface LocationFormat {

    /**
     * Returns a geo json version of the object
     *
     * @return feature or feature collection
     */
    GeoJsonObject toGeoJsonObject();

    /**
     * Returns a geojson string version of the object
     *
     * @return feature or feature collection as string
     * @throws JsonProcessingException if failure occurs during string transformation
     */
    String asGeoJsonString() throws JsonProcessingException;

    /**
     * Returns a gpx version of the object
     *
     * @return gpx
     */
    GPX toGpxObject();

    /**
     * Returns a gpx string version of the object
     *
     * @return gpx as string
     * @throws IOException if failure occurs during string transformation
     */
    String asGpxString() throws IOException;
}
