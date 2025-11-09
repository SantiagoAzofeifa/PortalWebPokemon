package cr.ac.una.portalwebpokeapi.model;


import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "login_audit")
public class LoginAudit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long userId;

    @Column(nullable=false, length=80)
    private String username;

    @Column(nullable=false, length=20)
    private String action; // LOGIN / LOGOUT

    @Column(nullable=false)
    private Instant timestamp = Instant.now();

    // getters/setters
}