package lu.hitec.dispng.locationexplorer.controller;

import lu.hitec.dispng.locationexplorer.service.LocationExplorerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/locations")
public class LocationExplorerController {

    @Autowired
    private LocationExplorerService service;

    // TODO: Add exception handling properly

    @GetMapping(params = {"unitId", "missionId"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public String getGeojsonObjectFromGpx(@RequestParam(value = "unitId") final String unitId,
                                          @RequestParam(value = "missionId") final String missionId,
                                          @RequestParam(value = "outputFormat", required = false, defaultValue = "GEOJSON") final String outputFormat,
                                          @RequestParam(value = "startDate", required = false, defaultValue = "0") final Long startDate,
                                          @RequestParam(value = "endDate", required = false, defaultValue = "0") final Long endDate,
                                          @RequestParam(value = "isOptimized", required = false, defaultValue = "true") final boolean isPathOptimizerEnabled,
                                          @RequestParam(value = "optimizationCoefficient", required = false, defaultValue = "3") final int optimizationCoefficient,
                                          @RequestParam(value = "isGpsJumpFiltered", required = false, defaultValue = "true") final boolean isGpsJumpFilterEnabled,
                                          @RequestParam(value = "isWayPointIncluded", required = false, defaultValue = "false") final boolean isWayPointIncluded) throws Exception {
        return service.convert(unitId, missionId, outputFormat, startDate, endDate, isPathOptimizerEnabled, optimizationCoefficient, isGpsJumpFilterEnabled, isWayPointIncluded);
    }
}