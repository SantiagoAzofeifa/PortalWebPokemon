package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.LoginAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAuditRepository extends JpaRepository<LoginAudit,Long> {}