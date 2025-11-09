package cr.ac.una.portalwebpokeapi.model;


import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long cartId;

    @Column(nullable=false)
    private Long productId;

    @Column(nullable=false)
    private int quantity;

    @Column(nullable=false)
    private double unitPrice;

    // getters/setters
}

