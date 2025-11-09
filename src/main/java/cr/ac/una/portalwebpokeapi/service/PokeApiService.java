package cr.ac.una.portalwebpokeapi.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

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

    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> listByType(String typeName) {
        String url = BASE + "/type/" + typeName;
        ResponseEntity<Map> res = rt.getForEntity(url, Map.class);
        Map<String,Object> body = res.getBody();
        if (body == null) return List.of();
        List<Map<String,Object>> arr = (List<Map<String,Object>>) body.get("pokemon");
        if (arr == null) return List.of();
        List<Map<String,Object>> out = new ArrayList<>();
        for (Map<String,Object> row : arr) {
            Object p = row.get("pokemon");
            if (p instanceof Map<?,?> pm) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("name", pm.get("name"));
                m.put("url", pm.get("url"));
                out.add(m);
            }
        }
        return out;
    }
}