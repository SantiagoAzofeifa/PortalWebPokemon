package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.PokemonRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PokemonRuleRepository extends JpaRepository<PokemonRule,Long> {
    Optional<PokemonRule> findByPokemonId(Long pokemonId);
}