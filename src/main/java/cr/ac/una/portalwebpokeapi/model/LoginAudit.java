package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Entity @Table(name="login_audit")
public class LoginAudit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable=false)
    private Long userId;

    @Setter
    @Column(nullable=false, length=80)
    private String username;

    @Setter
    @Column(nullable=false, length=20)
    private String action;

    @Setter
    @Column(nullable=false)
    private Instant timestamp = Instant.now();

}