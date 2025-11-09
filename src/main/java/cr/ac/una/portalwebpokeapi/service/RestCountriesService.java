package cr.ac.una.portalwebpokeapi.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class RestCountriesService {

    private final RestTemplate rt = new RestTemplate();
    private static final String BASE = "https://restcountries.com/v3.1";

    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> listAllCountries() {
        String url = BASE + "/all?fields=name,cca2,cca3,flags,region,subregion";
        ResponseEntity<Map[]> res = rt.getForEntity(url, Map[].class);
        return res.getBody() == null ? List.of() : Arrays.asList(res.getBody());
    }
}