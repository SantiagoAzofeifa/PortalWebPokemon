package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Entity @Table(name="delivery")
public class Delivery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable=false,unique=true)
    private Long orderId;

    @Setter
    @Column(length=30)
    private String method; // CORREO/MENSAJERO/CASILLERO

    @Setter
    @Column(length=300)
    private String address;

    @Setter
    private Instant scheduledDate;

    @Setter
    @Column(length=80)
    private String trackingCode;

    @Setter
    @Column(length=500)
    private String notes;

}