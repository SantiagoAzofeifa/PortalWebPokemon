package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity @Table(name="products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false,length=20)
    private Category category;

    @Column(nullable=false,length=120)
    private String name;

    @Column(length=255)
    private String imageUrl;

    @Column(nullable=false)
    private Double price;

    @Column(length=120)
    private String countryOfOrigin;

    @Column(length=255)
    private String availableCountriesCsv;

    @Column(length=255)
    private String bannedCountriesCsv;

    @Column(length=500)
    private String description;

}