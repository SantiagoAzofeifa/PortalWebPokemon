package cr.ac.una.portalwebpokeapi.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Servicio que obtiene información de países desde la API pública REST Countries (https://restcountries.com).
 *
 * Proporciona una lista resumida de todos los países, incluyendo:
 *  - name: nombre del país
 *  - cca2, cca3: códigos ISO
 *  - flags: información de bandera (URL, descripción)
 *  - region, subregion: ubicación geográfica
 *
 * Se utiliza para poblar catálogos o formularios de selección de país.
 */
@Service
public class RestCountriesService {

    /** Cliente HTTP para peticiones REST. */
    private final RestTemplate rt = new RestTemplate();

    /** URL base de la API REST Countries. */
    private static final String BASE = "https://restcountries.com/v3.1";

    /**
     * Obtiene la lista de todos los países con campos seleccionados.
     *
     * @return lista de mapas con datos básicos de los países.
     *         Si la respuesta es nula, retorna una lista vacía.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listAll() {
        String url = BASE + "/all?fields=name,cca2,cca3,flags,region,subregion";
        ResponseEntity<Map[]> res = rt.getForEntity(url, Map[].class);
        return res.getBody() == null ? List.of() : Arrays.asList(res.getBody());
    }
}
