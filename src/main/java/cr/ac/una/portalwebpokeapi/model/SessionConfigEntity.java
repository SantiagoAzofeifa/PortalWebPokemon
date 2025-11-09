package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Setter;

@Entity
@Table(name = "session_config")
public class SessionConfigEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable=false)
    private int timeoutSeconds;

    public Long getId() { return id; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
}