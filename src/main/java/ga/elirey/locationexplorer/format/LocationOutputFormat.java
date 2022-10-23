package ga.elirey.locationexplorer.format;

import ga.elirey.locationexplorer.data.FilterOptions;
import ga.elirey.locationexplorer.model.GPSTrack;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public enum LocationOutputFormat implements Formatter{

    GPX {
        @Override
        public String process(GPSTrack gpsTrack, FilterOptions options) throws IOException {
            return gpsTrack.asGeoJsonString(options.isWayPointIncluded()); // Refactor more intelligently
        }
    },
    GEOJSON {
        @Override
        public String process(GPSTrack gpsTrack, FilterOptions options) throws IOException {
            return gpsTrack.asGpxString();
        }
    }
}
