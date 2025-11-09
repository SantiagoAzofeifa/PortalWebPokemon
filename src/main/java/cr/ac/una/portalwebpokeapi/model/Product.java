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
    private String countryOfOrigin;

    @Column(length=255)
    private String availableCountriesCsv;

    @Column(length=255)
    private String bannedCountriesCsv;

    @Column(length=500)
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCountryOfOrigin() { return countryOfOrigin; }
    public void setCountryOfOrigin(String countryOfOrigin) { this.countryOfOrigin = countryOfOrigin; }
    public String getAvailableCountriesCsv() { return availableCountriesCsv; }
    public void setAvailableCountriesCsv(String availableCountriesCsv) { this.availableCountriesCsv = availableCountriesCsv; }
    public String getBannedCountriesCsv() { return bannedCountriesCsv; }
    public void setBannedCountriesCsv(String bannedCountriesCsv) { this.bannedCountriesCsv = bannedCountriesCsv; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}