package ga.elirey.locationexplorer.controller;

import ga.elirey.locationexplorer.data.FilterOptions;
import ga.elirey.locationexplorer.service.LocationExplorerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/locations")
@RequiredArgsConstructor
public class LocationExplorerController {

    private final LocationExplorerService service;

    @GetMapping(params = {"user", "context"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public String getGeojsonObjectFromGpx(@RequestParam(value = "user") final String user,
                                          @RequestParam(value = "context") final String context,
                                          @RequestParam(value = "format", required = false, defaultValue = "GEOJSON") final String format,
                                          @RequestParam(value = "startDate", required = false, defaultValue = "0") final Long startDate,
                                          @RequestParam(value = "endDate", required = false, defaultValue = "0") final Long endDate,
                                          @RequestBody FilterOptions filterOptions) throws Exception {
        return service.convert(user, context, format, startDate, endDate, filterOptions);
    }
}