package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa un producto del catálogo general del sistema.
 *
 * Un producto puede corresponder a un Pokémon, ítem o generación de juego,
 * dependiendo de su categoría. Contiene información básica como nombre,
 * precio, país de origen, disponibilidad y descripción.
 */
@Setter
@Getter
@Entity
@Table(name = "products")
public class Product {

    /** Identificador único del producto. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Categoría del producto (POKEMON, SPECIES, ITEMS, GENERATIONS). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    /** Nombre descriptivo del producto. */
    @Column(nullable = false, length = 120)
    private String name;

    /** URL de la imagen asociada al producto. */
    @Column(length = 255)
    private String imageUrl;

    /** Precio actual del producto. */
    @Column(nullable = false)
    private Double price;

    /** País de origen del producto. */
    @Column(length = 120)
    private String countryOfOrigin;

    /** Lista de países donde el producto está disponible (CSV). */
    @Column(length = 255)
    private String availableCountriesCsv;

    /** Lista de países donde el producto está restringido o prohibido (CSV). */
    @Column(length = 255)
    private String bannedCountriesCsv;

    /** Descripción textual del producto. */
    @Column(length = 500)
    private String description;
}
