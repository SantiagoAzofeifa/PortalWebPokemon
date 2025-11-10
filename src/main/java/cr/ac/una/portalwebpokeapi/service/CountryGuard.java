package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.Product;
import cr.ac.una.portalwebpokeapi.model.User;
import cr.ac.una.portalwebpokeapi.model.PokemonRule;
import cr.ac.una.portalwebpokeapi.repository.ProductRepository;
import cr.ac.una.portalwebpokeapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Valida políticas por país:
 * - Usuario solo compra artículos cuyo país de origen coincide con su país.
 * - Respeta listas available/banned cuando existan.
 * - Para recursos dinámicos (POKEMON/ITEM/GAME) garantiza que exista regla (auto-create).
 */
@Component
@RequiredArgsConstructor
public class CountryGuard {

    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final DynamicRuleService dynRules; // usa auto-asignación si no hay regla

    private static Set<String> parseCsv(String csv){
        if (csv==null || csv.isBlank()) return Set.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim).filter(s->!s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }

    public void assertUserCanBuyInternal(Long userId, Long productId){
        User u = userRepo.findById(userId).orElseThrow();
        if (u.getCountryCode()==null) throw new IllegalArgumentException("Configura tu país en el perfil");
        Product p = productRepo.findById(productId).orElseThrow();
        check(u.getCountryCode(), p.getCountryOfOrigin(), p.getAvailableCountriesCsv(), p.getBannedCountriesCsv());
    }

    public void assertUserCanBuyDynamic(Long userId, Long externalId, String category){
        User u = userRepo.findById(userId).orElseThrow();
        if (u.getCountryCode()==null) throw new IllegalArgumentException("Configura tu país en el perfil");

        // Garantiza que exista una regla; si no, se crea con país de origen auto-asignado
        PokemonRule r = dynRules.ensureRule(externalId, category);

        check(u.getCountryCode(), r.getOriginCountry(), r.getAvailableCountriesCsv(), r.getBannedCountriesCsv());
    }

    private void check(String userCountry, String origin, String availableCsv, String bannedCsv){
        String uc = userCountry.toUpperCase();
        if (origin==null || origin.isBlank())
            throw new IllegalArgumentException("Artículo sin país de origen");
        if (!origin.equalsIgnoreCase(uc))
            throw new IllegalArgumentException("Solo puedes comprar productos de tu país ("+uc+")");
        Set<String> banned = parseCsv(bannedCsv);
        if (banned.contains(uc))
            throw new IllegalArgumentException("Tu país está prohibido para este producto");
        Set<String> available = parseCsv(availableCsv);
        if (!available.isEmpty() && !available.contains(uc))
            throw new IllegalArgumentException("Tu país no está en la lista de disponibilidad");
    }
}