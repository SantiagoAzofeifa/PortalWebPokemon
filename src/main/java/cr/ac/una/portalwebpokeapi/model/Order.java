package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity @Table(name="orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable=false)
    private Long userId;

    @Column(nullable=false)
    private Instant createdAt = Instant.now();

    @Column(length=120)
    private String customerName;

    @Column(length=120)
    private String customerEmail;

    @Column(length=40)
    private String customerPhone;

    @Column(length=200)
    private String addressLine1;

    @Column(length=200)
    private String addressLine2;

    @Column(length=120)
    private String country;

    @Column(length=120)
    private String region;

    @Column(length=30, nullable=false)
    private String status = "CREATED";

}