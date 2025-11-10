package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Entidad que registra los eventos de autenticación del sistema.
 * Guarda información básica sobre inicios y cierres de sesión.
 *
 * Cada registro corresponde a una acción puntual (LOGIN o LOGOUT)
 * ejecutada por un usuario identificado por su ID y nombre.
 */
@Getter
@Entity
@Table(name = "login_audit")
public class LoginAudit {

    /** Identificador único del registro de auditoría. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del usuario que realizó la acción. */
    @Setter
    @Column(nullable = false)
    private Long userId;

    /** Nombre de usuario asociado a la acción. */
    @Setter
    @Column(nullable = false, length = 80)
    private String username;

    /** Acción registrada: LOGIN o LOGOUT. */
    @Setter
    @Column(nullable = false, length = 20)
    private String action;

    /** Marca temporal de cuándo ocurrió la acción. */
    @Setter
    @Column(nullable = false)
    private Instant timestamp = Instant.now();
}
