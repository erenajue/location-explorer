package ga.elirey.locationexplorer.format;

import ga.elirey.locationexplorer.data.FilterOptions;
import ga.elirey.locationexplorer.model.GPSTrack;

import java.io.IOException;

public interface Formatter {
    String process(final GPSTrack gpsTrack, final FilterOptions filterOptions) throws IOException;
}
