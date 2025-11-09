package cr.ac.una.portalwebpokeapi.model;


import jakarta.persistence.*;

@Entity
@Table(name = "session_config")
public class SessionConfigEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private int timeoutSeconds;

    // getters/setters
}