package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery,Long> {
    Optional<Delivery> findByOrderId(Long orderId);
}