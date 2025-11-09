package cr.ac.una.portalwebpokeapi.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RestCountriesService {

    private final WebClient client = WebClient.builder()
            .baseUrl("https://restcountries.com/v3.1")
            .build();

    public Mono<String> listAllCountries() {
        return client.get()
                .uri("/all?fields=name,cca2,cca3,flags,region,subregion")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);
    }
}
