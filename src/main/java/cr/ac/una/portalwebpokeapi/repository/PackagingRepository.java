package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.Packaging;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PackagingRepository extends JpaRepository<Packaging,Long> {
    Optional<Packaging> findByOrderId(Long orderId);
}