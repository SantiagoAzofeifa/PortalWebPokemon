package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity @Table(name="payment")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false,unique=true)
    private Long orderId;

    @Column(length=10)
    private String currency;

    private Integer itemCount;

    private Double grossAmount;
    private Double netAmount;

    @Column(length=30)
    private String method; // Tarjeta, etc.

    private Instant paidAt;

    @Column(length=500)
    private String notes;

}