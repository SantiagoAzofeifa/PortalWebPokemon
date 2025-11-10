package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.PokemonRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link PokemonRule}.
 *
 * Permite realizar operaciones CRUD sobre las reglas asociadas a los Pokémon.
 * Incluye un método de consulta derivado para obtener las reglas
 * vinculadas a un Pokémon específico mediante su identificador numérico.
 */
public interface PokemonRuleRepository extends JpaRepository<PokemonRule, Long> {

    /**
     * Busca la regla configurada para un Pokémon dado.
     *
     * @param pokemonId ID numérico del Pokémon (según la PokeAPI).
     * @return un {@link Optional} con la regla si existe.
     */
    Optional<PokemonRule> findByPokemonId(Long pokemonId);
}
