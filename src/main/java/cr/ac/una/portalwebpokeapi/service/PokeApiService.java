package cr.ac.una.portalwebpokeapi.service;


import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PokeApiService {

    private final WebClient client = WebClient.builder()
            .baseUrl("https://pokeapi.co/api/v2")
            .build();

    public Mono<String> listPokemon(int limit, int offset) {
        return client.get()
                .uri(uri -> uri.path("/pokemon").queryParam("limit", limit).queryParam("offset", offset).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getPokemon(String nameOrId) {
        return client.get()
                .uri("/pokemon/{id}", nameOrId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);
    }
}
