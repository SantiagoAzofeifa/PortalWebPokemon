package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity @Table(name="warehouse")
public class Warehouse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private Long orderId;

    private Instant inDate;
    private Instant outDate;
    private Boolean stockChecked;
    private Integer stockQty;

    @Column(length=120)
    private String location;

    @Column(length=120)
    private String originCountry;

    @Column(length=500)
    private String notes;


}