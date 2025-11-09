/*
package cr.ac.una.portalwebpokeapi.config;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class PokeApiClient {
    private static final String BASE = "https://pokeapi.co/api/v2";
    private final RestTemplate rest = new RestTemplate();

    public Map listPokemon(int limit, int offset) {
        return rest.getForObject(BASE + "/pokemon?limit=" + limit + "&offset=" + offset, Map.class);
    }

    public Map getPokemon(String id) {
        return rest.getForObject(BASE + "/pokemon/" + id, Map.class);
    }

    public Map getSpecies(String id) {
        return rest.getForObject(BASE + "/pokemon-species/" + id, Map.class);
    }

    public Map listItems(int limit, int offset) {
        return rest.getForObject(BASE + "/item?limit=" + limit + "&offset=" + offset, Map.class);
    }

    public Map listGenerations() {
        return rest.getForObject(BASE + "/generation", Map.class);
    }
}
*/
