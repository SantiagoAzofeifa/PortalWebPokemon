package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.service.RestCountriesService;
import cr.ac.una.portalwebpokeapi.service.config.CountryConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/catalog")
public class CountriesController {

    private final RestCountriesService restCountries;
    private final CountryConfigService cfg;

    public CountriesController(RestCountriesService restCountries, CountryConfigService cfg) {
        this.restCountries = restCountries;
        this.cfg = cfg;
    }
    /** Subset permitido para selects (registro y productos). */
    @GetMapping("/allowed-countries")
    public ResponseEntity<?> allowed(){
        return ResponseEntity.ok(cfg.allowedList());
    }
}