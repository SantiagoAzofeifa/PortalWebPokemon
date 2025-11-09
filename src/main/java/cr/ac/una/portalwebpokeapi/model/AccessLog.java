package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "access_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Type type;

    @Column(length = 64)
    private String ip;

    private Instant at;

    public enum Type {
        LOGIN, LOGOUT
    }

    // Opcionales
    public static AccessLog login(Long userId, String ip) {
        return AccessLog.builder().userId(userId).type(Type.LOGIN).ip(ip).at(Instant.now()).build();
    }

    public static AccessLog logout(Long userId, String ip) {
        return AccessLog.builder().userId(userId).type(Type.LOGOUT).ip(ip).at(Instant.now()).build();
    }
}
