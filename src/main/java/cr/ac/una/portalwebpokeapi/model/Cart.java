package cr.ac.una.portalwebpokeapi.model;


import jakarta.persistence.*;

@Entity
@Table(name = "carts")
public class Cart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long userId;

    // getters/setters
}