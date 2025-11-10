package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.PokemonRule;
import cr.ac.una.portalwebpokeapi.repository.PokemonRuleRepository;
import cr.ac.una.portalwebpokeapi.service.config.CountryConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Garantiza que cada recurso dinámico (POKEMON/ITEM/GAME) tenga una regla de país.
 * Si no existe, la crea automáticamente con un país de origen asignado en forma
 * determinista a partir de (categoría + id) sobre el subset permitido.
 *
 * Nota importante: para evitar colisiones de IDs entre categorías, se
 * codifica una clave compuesta (category,id) dentro de pokemon_id:
 *  - POKEMON -> 1_000_000_000_000L + id
 *  - ITEM    -> 2_000_000_000_000L + id
 *  - GAME    -> 3_000_000_000_000L + id
 */
@Service
@RequiredArgsConstructor
public class DynamicRuleService {

    private final PokemonRuleRepository repo;
    private final CountryConfigService countryCfg;

    @Transactional
    public PokemonRule ensureRule(Long externalId, String category) {
        long key = compositeKey(externalId, category);
        return repo.findByPokemonId(key).orElseGet(() -> {
            String origin = assignOrigin(externalId, category);
            PokemonRule r = new PokemonRule();
            r.setPokemonId(key); // clave compuesta codificada
            r.setOriginCountry(origin);
            r.setAvailableCountriesCsv(origin); // por defecto disponible solo en origen
            r.setBannedCountriesCsv(null);
            r.setNotes("auto:" + category);
            return repo.save(r);
        });
    }

    /**
     * Asigna el país de origen de forma determinista a partir del hash de (category:id)
     * dentro del subset permitido. Esto divide el catálogo completo entre los países de forma estable.
     */
    private String assignOrigin(Long id, String category) {
        List<String> codes = countryCfg.allowedList(); // orden estable
        int idx = Math.floorMod((category + ":" + id).hashCode(), codes.size());
        return codes.get(idx);
    }

    /**
     * Codifica (categoría, id) en un long para evitar colisiones entre categorías en la columna pokemon_id.
     */
    private long compositeKey(Long id, String category) {
        long base = switch (category == null ? "" : category.trim().toUpperCase()) {
            case "POKEMON" -> 1_000_000_000_000L;
            case "ITEM"    -> 2_000_000_000_000L;
            case "GAME"    -> 3_000_000_000_000L;
            default        -> 9_000_000_000_000L; // fallback
        };
        return base + (id == null ? 0L : id);
    }
}