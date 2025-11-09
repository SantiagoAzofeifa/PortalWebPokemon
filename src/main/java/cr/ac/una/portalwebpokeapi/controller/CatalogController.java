package cr.ac.una.portalwebpokeapi.controller;


import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.model.Category;
import cr.ac.una.portalwebpokeapi.repository.ProductRepository;
import cr.ac.una.portalwebpokeapi.service.PokeApiService;
import cr.ac.una.portalwebpokeapi.service.RestCountriesService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final ProductRepository products;
    private final PokeApiService poke;
    private final RestCountriesService countries;
    private final SessionManager sessions;

    public CatalogController(ProductRepository products, PokeApiService poke, RestCountriesService countries, SessionManager sessions) {
        this.products = products;
        this.poke = poke;
        this.countries = countries;
        this.sessions = sessions;
    }

    @GetMapping("/products")
    public ResponseEntity<?> listProducts(@RequestParam Category category,
                                          @RequestParam(defaultValue="0") int page,
                                          @RequestParam(defaultValue="12") int size) {
        var pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        return ResponseEntity.ok(products.findByCategory(category, pageable));
    }

    @GetMapping("/countries")
    public Mono<String> listCountries() {
        return countries.listAllCountries();
    }

    // Endpoints de apoyo para “semillas” desde PokeAPI (opcional para poblar productos)
    @GetMapping("/poke/list")
    public Mono<String> pokeList(@RequestParam(defaultValue="20") int limit,
                                 @RequestParam(defaultValue="0") int offset) {
        return poke.listPokemon(limit, offset);
    }

    @GetMapping("/poke/{id}")
    public Mono<String> pokeGet(@PathVariable String id) {
        return poke.getPokemon(id);
    }
}