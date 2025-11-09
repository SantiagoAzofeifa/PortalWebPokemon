package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.model.PokemonRule;
import cr.ac.una.portalwebpokeapi.repository.PokemonRuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController @RequestMapping("/api/admin/rules")
public class AdminRulesController {
    private final PokemonRuleRepository repo;
    private final SessionManager sessions;

    public AdminRulesController(PokemonRuleRepository repo, SessionManager sessions) {
        this.repo = repo; this.sessions = sessions;
    }

    private void requireAdmin(String token){
        var me = sessions.get(token);
        if (me==null) throw new SecurityException("UNAUTHORIZED");
        if (!"ADMIN".equals(me.role)) throw new SecurityException("FORBIDDEN");
    }

    @GetMapping("/{pokemonId}")
    public ResponseEntity<?> get(@RequestHeader("X-SESSION-TOKEN") String token, @PathVariable Long pokemonId){
        requireAdmin(token);
        return ResponseEntity.ok(repo.findByPokemonId(pokemonId).orElse(null));
    }

    @PutMapping("/{pokemonId}")
    public ResponseEntity<?> upsert(@RequestHeader("X-SESSION-TOKEN") String token,
                                    @PathVariable Long pokemonId,
                                    @RequestBody PokemonRule body){
        requireAdmin(token);
        PokemonRule r = repo.findByPokemonId(pokemonId).orElse(new PokemonRule());
        r.setPokemonId(pokemonId);
        if (body.getOriginCountry()!=null) r.setOriginCountry(body.getOriginCountry());
        if (body.getAvailableCountriesCsv()!=null) r.setAvailableCountriesCsv(body.getAvailableCountriesCsv());
        if (body.getBannedCountriesCsv()!=null) r.setBannedCountriesCsv(body.getBannedCountriesCsv());
        if (body.getNotes()!=null) r.setNotes(body.getNotes());
        return ResponseEntity.ok(repo.save(r));
    }

    @DeleteMapping("/{pokemonId}")
    public ResponseEntity<?> delete(@RequestHeader("X-SESSION-TOKEN") String token, @PathVariable Long pokemonId){
        requireAdmin(token);
        repo.findByPokemonId(pokemonId).ifPresent(repo::delete);
        return ResponseEntity.ok(Map.of("ok",true));
    }
}