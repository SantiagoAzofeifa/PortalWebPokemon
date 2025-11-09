package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity @Table(name="pokemon_rules")
public class PokemonRule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private Long pokemonId; // id numérico de PokeAPI

    @Column(length=120)
    private String originCountry;

    @Column(length=255)
    private String availableCountriesCsv;

    @Column(length=255)
    private String bannedCountriesCsv;

    @Column(length=500)
    private String notes;

}