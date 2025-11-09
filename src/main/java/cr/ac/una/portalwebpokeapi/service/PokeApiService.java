package cr.ac.una.portalwebpokeapi.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class PokeApiService {
    private final RestTemplate rt = new RestTemplate();
    private static final String BASE = "https://pokeapi.co/api/v2";

    @SuppressWarnings("unchecked")
    public Map<String,Object> listPokemon(int limit, int offset) {
        String url = BASE + "/pokemon?limit=" + limit + "&offset=" + offset;
        ResponseEntity<Map> res = rt.getForEntity(url, Map.class);
        return res.getBody();
    }

    @SuppressWarnings("unchecked")
    public Map<String,Object> getPokemon(String nameOrId) {
        String url = BASE + "/pokemon/" + nameOrId;
        ResponseEntity<Map> res = rt.getForEntity(url, Map.class);
        return res.getBody();
    }

    // Item catalog
    @SuppressWarnings("unchecked")
    public Map<String,Object> listItems(int limit, int offset) {
        String url = BASE + "/item?limit=" + limit + "&offset=" + offset;
        ResponseEntity<Map> res = rt.getForEntity(url, Map.class);
        return res.getBody();
    }

    @SuppressWarnings("unchecked")
    public Map<String,Object> getItem(String nameOrId) {
        String url = BASE + "/item/" + nameOrId;
        ResponseEntity<Map> res = rt.getForEntity(url, Map.class);
        return res.getBody();
    }

    // "Games" usaremos 'version' como catálogo de juegos
    @SuppressWarnings("unchecked")
    public Map<String,Object> listVersions(int limit, int offset) {
        String url = BASE + "/version?limit=" + limit + "&offset=" + offset;
        ResponseEntity<Map> res = rt.getForEntity(url, Map.class);
        return res.getBody();
    }

    @SuppressWarnings("unchecked")
    public Map<String,Object> getVersion(String nameOrId) {
        String url = BASE + "/version/" + nameOrId;
        ResponseEntity<Map> res = rt.getForEntity(url, Map.class);
        return res.getBody();
    }
}