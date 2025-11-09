package cr.ac.una.portalwebpokeapi.model;


import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Category category;

    @Column(nullable=false, length=120)
    private String name;

    @Column(length=255)
    private String imageUrl;

    @Column(nullable=false)
    private Double price;

    @Column(length=120)
    private String countryOfOrigin; // Código/Nombre país de construcción

    @Column(length=255)
    private String availableCountriesCsv; // lista de países donde está disponible

    @Column(length=255)
    private String bannedCountriesCsv; // países donde no se permite vender

    @Column(length=500)
    private String description;

    // getters/setters
}