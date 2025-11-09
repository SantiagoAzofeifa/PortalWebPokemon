package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.service.PokeApiService;
import cr.ac.una.portalwebpokeapi.service.RestCountriesService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final PokeApiService poke;
    private final RestCountriesService countries;

    public CatalogController(PokeApiService poke, RestCountriesService countries) {
        this.poke = poke;
        this.countries = countries;
    }

    @GetMapping(value="/countries", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listCountries() {
        return ResponseEntity.ok(countries.listAllCountries()); // Devuelve List<Map> o similar
    }

    @GetMapping(value="/poke/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> pokeList(@RequestParam(defaultValue="20") int limit,
                                      @RequestParam(defaultValue="0") int offset) {
        return ResponseEntity.ok(poke.listPokemon(limit, offset));
    }

    @GetMapping(value="/poke/{idOrName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> pokeGet(@PathVariable String idOrName) {
        String p = poke.getPokemon(idOrName).toString();
        return ResponseEntity.ok(p);
    }
}