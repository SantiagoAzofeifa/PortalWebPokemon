package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que define la configuración de sesión activa del sistema.
 *
 * Permite persistir en base de datos el tiempo máximo de inactividad (timeout)
 * configurado para las sesiones de usuario, lo que permite modificarlo dinámicamente
 * sin necesidad de reiniciar la aplicación.
 */
@Getter
@Entity
@Table(name = "session_config")
public class SessionConfigEntity {

    /** Identificador único del registro de configuración. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tiempo máximo de inactividad permitido en segundos antes de expirar una sesión. */
    @Setter
    @Column(nullable = false)
    private int timeoutSeconds;
}
