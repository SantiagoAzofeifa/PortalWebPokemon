package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.service.PokeApiService;
import cr.ac.una.portalwebpokeapi.service.PokeCatalogService;
import cr.ac.una.portalwebpokeapi.service.RestCountriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final PokeApiService poke;
    private final RestCountriesService countries;
    private final PokeCatalogService catalog;

    public CatalogController(PokeApiService poke, RestCountriesService countries, PokeCatalogService catalog) {
        this.poke = poke; this.countries = countries; this.catalog = catalog;
    }

    @GetMapping("/countries")
    public ResponseEntity<?> countries(){
        return ResponseEntity.ok(countries.listAll());
    }

    // Tarjetas por categoría específica
    @GetMapping("/pokemon-cards")
    public ResponseEntity<?> pokemonCards(@RequestParam(required=false) Integer limit,
                                          @RequestParam(required=false) Integer offset,
                                          @RequestParam(required=false) String query,
                                          @RequestParam(required=false) String type) {
        return ResponseEntity.ok(catalog.listPokemonCards(limit, offset, query, type));
    }

    @GetMapping("/item-cards")
    public ResponseEntity<?> itemCards(@RequestParam(required=false) Integer limit,
                                       @RequestParam(required=false) Integer offset,
                                       @RequestParam(required=false) String query) {
        return ResponseEntity.ok(catalog.listItemCards(limit, offset, query));
    }

    @GetMapping("/game-cards")
    public ResponseEntity<?> gameCards(@RequestParam(required=false) Integer limit,
                                       @RequestParam(required=false) Integer offset,
                                       @RequestParam(required=false) String query) {
        return ResponseEntity.ok(catalog.listGameCards(limit, offset, query));
    }

    // Agregador: ALL | POKEMON | ITEM | GAME
    @GetMapping("/cards")
    public ResponseEntity<?> unifiedCards(@RequestParam(required=false) Integer limit,
                                          @RequestParam(required=false) Integer offset,
                                          @RequestParam(required=false) String query,
                                          @RequestParam(required=false) String type,
                                          @RequestParam(required=false, defaultValue = "ALL") String category) {
        return ResponseEntity.ok(catalog.listUnifiedCards(limit, offset, query, type, category));
    }
}