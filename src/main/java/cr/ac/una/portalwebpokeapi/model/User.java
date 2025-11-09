package cr.ac.una.portalwebpokeapi.model;


import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique = true, length = 80)
    private String username;

    @Column(nullable=false, length = 120)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length = 20)
    private UserRole role = UserRole.USER;

    @Column(nullable=false)
    private boolean active = true;

    @Column
    private Instant createdAt = Instant.now();

    // getters/setters
    // ...
}