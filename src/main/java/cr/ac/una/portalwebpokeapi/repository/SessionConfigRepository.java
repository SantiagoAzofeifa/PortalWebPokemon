package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.SessionConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionConfigRepository extends JpaRepository<SessionConfigEntity, Long> {
    Optional<SessionConfigEntity> findTopByOrderByIdAsc();
}