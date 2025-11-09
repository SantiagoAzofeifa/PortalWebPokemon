package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.LoginAudit;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad {@link LoginAudit}.
 *
 * Permite registrar y consultar eventos de autenticación
 * (LOGIN y LOGOUT) asociados a los usuarios del sistema.
 */
public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
}
