package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que define las reglas asociadas a un Pokémon específico.
 *
 * Cada Pokémon puede tener restricciones o configuraciones personalizadas,
 * tales como país de origen, países donde está disponible o prohibido,
 * y notas adicionales. Se usa para modular políticas comerciales o de catálogo.
 */
@Getter
@Setter
@Entity
@Table(name = "pokemon_rules")
public class PokemonRule {

    /** Identificador único del registro de reglas. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID numérico del Pokémon según la PokeAPI (único por especie). */
    @Column(nullable = false, unique = true)
    private Long pokemonId;

    /** País de origen principal del Pokémon. */
    @Column(length = 120)
    private String originCountry;

    /** Lista de países donde el Pokémon está disponible (formato CSV). */
    @Column(length = 255)
    private String availableCountriesCsv;

    /** Lista de países donde el Pokémon está restringido o prohibido (formato CSV). */
    @Column(length = 255)
    private String bannedCountriesCsv;

    /** Notas o comentarios adicionales sobre las reglas aplicadas. */
    @Column(length = 500)
    private String notes;
}
