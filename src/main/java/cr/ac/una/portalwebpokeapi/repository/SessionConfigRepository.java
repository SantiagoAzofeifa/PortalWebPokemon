package cr.ac.una.portalwebpokeapi.repository;


import cr.ac.una.portalwebpokeapi.model.SessionConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionConfigRepository extends JpaRepository<SessionConfigEntity, Long> {
}