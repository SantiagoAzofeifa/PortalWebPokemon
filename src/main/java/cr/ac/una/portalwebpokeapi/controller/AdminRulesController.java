package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.model.PokemonRule;
import cr.ac.una.portalwebpokeapi.repository.PokemonRuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para administrar reglas por Pokémon.
 * Endpoints protegidos para rol ADMIN. Requiere header X-SESSION-TOKEN.
 *
 * Base path: /api/admin/rules
 */
@RestController
@RequestMapping("/api/admin/rules")
public class AdminRulesController {

    private final PokemonRuleRepository repo;  // Acceso a persistencia de reglas
    private final SessionManager sessions;     // Gestión de sesión y rol

    public AdminRulesController(PokemonRuleRepository repo, SessionManager sessions) {
        this.repo = repo;
        this.sessions = sessions;
    }

    /**
     * Verifica que el token de sesión sea válido y que el rol sea ADMIN.
     * Lanza SecurityException con “UNAUTHORIZED” o “FORBIDDEN” para que el
     * RestExceptionHandler traduzca a 401/403 respectivamente.
     *
     * @param token encabezado X-SESSION-TOKEN
     */
    private void requireAdmin(String token){
        var me = sessions.get(token);
        if (me == null) throw new SecurityException("UNAUTHORIZED");
        if (!"ADMIN".equals(me.role)) throw new SecurityException("FORBIDDEN");
    }

    /**
     * Obtiene la regla asociada a un Pokémon por su ID.
     *
     * GET /api/admin/rules/{pokemonId}
     *
     * @param token      encabezado X-SESSION-TOKEN
     * @param pokemonId  identificador del Pokémon
     * @return 200 con la regla o null si no existe
     */
    @GetMapping("/{pokemonId}")
    public ResponseEntity<?> get(@RequestHeader("X-SESSION-TOKEN") String token,
                                 @PathVariable Long pokemonId){
        requireAdmin(token);
        return ResponseEntity.ok(repo.findByPokemonId(pokemonId).orElse(null));
    }

    /**
     * Crea o actualiza (upsert) la regla para un Pokémon.
     * Solo sobreescribe campos presentes en el body (parches parciales).
     *
     * PUT /api/admin/rules/{pokemonId}
     *
     * @param token      encabezado X-SESSION-TOKEN
     * @param pokemonId  identificador del Pokémon
     * @param body       datos de la regla a persistir
     * @return 200 con la entidad guardada
     */
    @PutMapping("/{pokemonId}")
    public ResponseEntity<?> upsert(@RequestHeader("X-SESSION-TOKEN") String token,
                                    @PathVariable Long pokemonId,
                                    @RequestBody PokemonRule body){
        requireAdmin(token);

        // Busca existente o instancia nueva para upsert
        PokemonRule r = repo.findByPokemonId(pokemonId).orElse(new PokemonRule());
        r.setPokemonId(pokemonId);

        // Aplica solo valores no nulos del body
        if (body.getOriginCountry() != null) r.setOriginCountry(body.getOriginCountry());
        if (body.getAvailableCountriesCsv() != null) r.setAvailableCountriesCsv(body.getAvailableCountriesCsv());
        if (body.getBannedCountriesCsv() != null) r.setBannedCountriesCsv(body.getBannedCountriesCsv());
        if (body.getNotes() != null) r.setNotes(body.getNotes());

        return ResponseEntity.ok(repo.save(r));
    }

    /**
     * Elimina la regla asociada a un Pokémon si existe.
     *
     * DELETE /api/admin/rules/{pokemonId}
     *
     * @param token      encabezado X-SESSION-TOKEN
     * @param pokemonId  identificador del Pokémon
     * @return 200 {"ok": true} siempre, idempotente
     */
    @DeleteMapping("/{pokemonId}")
    public ResponseEntity<?> delete(@RequestHeader("X-SESSION-TOKEN") String token,
                                    @PathVariable Long pokemonId){
        requireAdmin(token);
        repo.findByPokemonId(pokemonId).ifPresent(repo::delete);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
