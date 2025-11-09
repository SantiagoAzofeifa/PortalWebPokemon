package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity @Table(name="cart_items")
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long cartId;

    @Column(nullable=false)
    private Long productId; // ahora representa el id del Pokémon (no FK)

    @Column(nullable=false)
    private int quantity;

    @Column(nullable=false)
    private double unitPrice;

}