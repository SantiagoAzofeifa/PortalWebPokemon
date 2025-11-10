package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.SessionConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link SessionConfigEntity}.
 *
 * Permite acceder y administrar la configuración de tiempo de sesión persistida
 * en la base de datos. Generalmente existe un solo registro activo que define
 * el tiempo de expiración de las sesiones en segundos.
 */
public interface SessionConfigRepository extends JpaRepository<SessionConfigEntity, Long> {

    /**
     * Obtiene el primer registro de configuración de sesión disponible,
     * ordenado por su identificador.
     *
     * @return un {@link Optional} con la configuración actual, si existe.
     */
    Optional<SessionConfigEntity> findTopByOrderByIdAsc();
}
