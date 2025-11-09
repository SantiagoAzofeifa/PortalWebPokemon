package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse,Long> {
    Optional<Warehouse> findByOrderId(Long orderId);
}