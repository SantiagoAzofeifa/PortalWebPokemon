package cr.ac.una.portalwebpokeapi.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Servicio que interactúa con la API pública de PokeAPI (https://pokeapi.co).
 *
 * Proporciona métodos para obtener información de:
 * - Pokémon individuales o listados.
 * - Ítems (objetos del juego).
 * - Versiones (juegos).
 *
 * Utiliza {@link RestTemplate} para realizar peticiones HTTP REST.
 * Todos los métodos retornan un mapa genérico con los datos JSON obtenidos.
 */
@Service
public class PokeApiService {

    /** Cliente HTTP para realizar peticiones REST. */
    private final RestTemplate rt = new RestTemplate();

    /** URL base de la API pública de PokeAPI. */
    private static final String BASE = "https://pokeapi.co/api/v2";

    /**
     * Lista los Pokémon con paginación.
     *
     * @param limit número máximo de resultados.
     * @param offset desplazamiento inicial (para paginación).
     * @return mapa con la respuesta de la API (contiene "results", "next", "previous", etc.).
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> listPokemon(int limit, int offset) {
        String url = BASE + "/pokemon?limit=" + limit + "&offset=" + offset;
        ResponseEntity<Map> res = rt.getForEntity(url, Map.class);
        return res.getBody();
    }

    /**
     * Obtiene la información detallada de un Pokémon por nombre o ID.
     *
     * @param nameOrId nombre o identificador numérico del Pokémon.
     * @return mapa con la información detallada del Pokémon.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPokemon(String nameOrId) {
        String url = BASE + "/pokemon/" + nameOrId;
        ResponseEntity<Map> res = rt.getForEntity(url, Map.class);
        return res.getBody();
    }

    /**
     * Lista ítems disponibles en el catálogo.
     *
     * @param limit número máximo de resultados.
     * @param offset desplazamiento inicial (para paginación).
     * @return mapa con la respuesta del endpoint de ítems.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> listItems(int limit, int offset) {
        String url = BASE + "/item?limit=" + limit + "&offset=" + offset;
        ResponseEntity<Map> res = rt.getForEntity(url, Map.class);
        return res.getBody();
    }

    /**
     * Obtiene información detallada de un ítem por nombre o ID.
     *
     * @param nameOrId nombre o identificador del ítem.
     * @return mapa con la información detallada del ítem.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getItem(String nameOrId) {
        String url = BASE + "/item/" + nameOrId;
        ResponseEntity<Map> res = rt.getForEntity(url, Map.class);
        return res.getBody();
    }

    /**
     * Lista versiones (juegos) disponibles en la API.
     *
     * @param limit número máximo de resultados.
     * @param offset desplazamiento inicial.
     * @return mapa con la lista de versiones.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> listVersions(int limit, int offset) {
        String url = BASE + "/version?limit=" + limit + "&offset=" + offset;
        ResponseEntity<Map> res = rt.getForEntity(url, Map.class);
        return res.getBody();
    }

    /**
     * Obtiene información detallada de una versión (juego).
     *
     * @param nameOrId nombre o identificador de la versión.
     * @return mapa con la información detallada del juego.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getVersion(String nameOrId) {
        String url = BASE + "/version/" + nameOrId;
        ResponseEntity<Map> res = rt.getForEntity(url, Map.class);
        return res.getBody();
    }
}
