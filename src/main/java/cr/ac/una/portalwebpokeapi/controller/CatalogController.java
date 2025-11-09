package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.service.PokeApiService;
import cr.ac.una.portalwebpokeapi.service.RestCountriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/catalog")
public class CatalogController {
    private final PokeApiService poke;
    private final RestCountriesService countries;
    public CatalogController(PokeApiService poke, RestCountriesService countries){
        this.poke=poke; this.countries=countries;
    }

    @GetMapping("/countries")
    public ResponseEntity<?> countries(){
        return ResponseEntity.ok(countries.listAll());
    }

    @GetMapping("/poke/list")
    public ResponseEntity<?> pokeList(@RequestParam(defaultValue="20") int limit,
                                      @RequestParam(defaultValue="0") int offset){
        return ResponseEntity.ok(poke.listPokemon(limit, offset));
    }

    @GetMapping("/poke/{idOrName}")
    public ResponseEntity<?> pokeDetail(@PathVariable String idOrName){
        return ResponseEntity.ok(poke.getPokemon(idOrName));
    }
}