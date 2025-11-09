package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity @Table(name="ps_records")
public class PsRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long orderId;

    @Column(nullable=false)
    private Long productId;

    @Column(length=200)
    private String reason;

    private Boolean resolved = false;

    @Column(nullable=false)
    private Instant createdAt = Instant.now();


}