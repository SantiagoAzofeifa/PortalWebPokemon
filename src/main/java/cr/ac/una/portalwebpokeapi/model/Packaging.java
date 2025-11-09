package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity @Table(name="packaging")
public class Packaging {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false,unique=true)
    private Long orderId;

    @Column(length=20)
    private String size; // GRANDE/MEDIANO/PEQUEÑO

    @Column(length=20)
    private String type; // LIBRE/AL_VACIO/RELLENO

    @Column(length=120)
    private String materials;

    private Boolean fragile;

    @Column(length=500)
    private String notes;


}