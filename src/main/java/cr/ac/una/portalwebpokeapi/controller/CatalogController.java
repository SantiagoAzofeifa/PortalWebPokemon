package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.service.PokeApiService;
import cr.ac.una.portalwebpokeapi.service.PokeCatalogService;
import cr.ac.una.portalwebpokeapi.service.RestCountriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Catálogo público de datos.
 * Endpoints de lectura que agregan resultados desde servicios externos
 * (PokeAPI, RestCountries) y un servicio de catálogo propio.
 *
 * Base: /api/catalog
 */
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final PokeApiService poke;              // Acceso directo a PokeAPI si se requiere
    private final RestCountriesService countries;   // Listado de países y metadatos
    private final PokeCatalogService catalog;       // Agregador/normalizador de tarjetas

    public CatalogController(PokeApiService poke,
                             RestCountriesService countries,
                             PokeCatalogService catalog) {
        this.poke = poke;
        this.countries = countries;
        this.catalog = catalog;
    }

    /**
     * Devuelve el listado de países soportados por la app.
     * GET /api/catalog/countries
     */
    @GetMapping("/countries")
    public ResponseEntity<?> countries(){
        return ResponseEntity.ok(countries.listAll());
    }

    // ------------------- Tarjetas por categoría específica -------------------

    /**
     * Tarjetas de Pokémon con paginación y filtros.
     * GET /api/catalog/pokemon-cards
     * @param limit  tamaño de página opcional
     * @param offset desplazamiento opcional
     * @param query  filtro por nombre/ID opcional
     * @param type   filtro por tipo (e.g., "electric") opcional
     */
    @GetMapping("/pokemon-cards")
    public ResponseEntity<?> pokemonCards(@RequestParam(required=false) Integer limit,
                                          @RequestParam(required=false) Integer offset,
                                          @RequestParam(required=false) String query,
                                          @RequestParam(required=false) String type) {
        return ResponseEntity.ok(catalog.listPokemonCards(limit, offset, query, type));
    }

    /**
     * Tarjetas de ítems con paginación y filtro por texto.
     * GET /api/catalog/item-cards
     */
    @GetMapping("/item-cards")
    public ResponseEntity<?> itemCards(@RequestParam(required=false) Integer limit,
                                       @RequestParam(required=false) Integer offset,
                                       @RequestParam(required=false) String query) {
        return ResponseEntity.ok(catalog.listItemCards(limit, offset, query));
    }

    /**
     * Tarjetas de juegos con paginación y filtro por texto.
     * GET /api/catalog/game-cards
     */
    @GetMapping("/game-cards")
    public ResponseEntity<?> gameCards(@RequestParam(required=false) Integer limit,
                                       @RequestParam(required=false) Integer offset,
                                       @RequestParam(required=false) String query) {
        return ResponseEntity.ok(catalog.listGameCards(limit, offset, query));
    }

    // --------------------------- Agregador unificado --------------------------

    /**
     * Tarjetas unificadas. Agrega categorías: ALL | POKEMON | ITEM | GAME.
     * GET /api/catalog/cards
     * @param limit    tamaño de página opcional
     * @param offset   desplazamiento opcional
     * @param query    filtro por texto opcional
     * @param type     usado para POKEMON (tipo elemental) opcional
     * @param category categoría a consultar. Por defecto "ALL"
     */
    @GetMapping("/cards")
    public ResponseEntity<?> unifiedCards(@RequestParam(required=false) Integer limit,
                                          @RequestParam(required=false) Integer offset,
                                          @RequestParam(required=false) String query,
                                          @RequestParam(required=false) String type,
                                          @RequestParam(required=false, defaultValue = "ALL") String category) {
        return ResponseEntity.ok(catalog.listUnifiedCards(limit, offset, query, type, category));
    }
}
