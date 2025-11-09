package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "access_log")
public class AccessLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable=false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable=false)
    private Type type;

    @Column(length = 64)
    private String ip;

    @Column(nullable=false)
    private Instant at = Instant.now();

    public enum Type { LOGIN, LOGOUT }

    public static AccessLog login(Long userId, String ip) {
        AccessLog l = new AccessLog();
        l.userId = userId; l.type = Type.LOGIN; l.ip = ip; l.at = Instant.now();
        return l;
    }
    public static AccessLog logout(Long userId, String ip) {
        AccessLog l = new AccessLog();
        l.userId = userId; l.type = Type.LOGOUT; l.ip = ip; l.at = Instant.now();
        return l;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }

    public Type getType() { return type; }

    public String getIp() { return ip; }

    public Instant getAt() { return at; }
}