package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity @Table(name="order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long orderId;

    @Column(nullable=false)
    private Long productId;

    @Column(nullable=false)
    private int quantity;

    @Column(nullable=false)
    private double unitPrice;

}