package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "login_audit")
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

    public Long getId() { return id; }
    public Long getUserId() { return userId; }

    public String getUsername() { return username; }

    public String getAction() { return action; }

    public Instant getTimestamp() { return timestamp; }
}