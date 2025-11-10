package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Delivery}.
 *
 * Permite gestionar los registros de entrega (delivery) asociados a las órdenes.
 * Proporciona consultas derivadas para obtener la información de entrega
 * vinculada a una orden específica.
 */
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    /**
     * Busca la información de entrega asociada a una orden.
     *
     * @param orderId ID de la orden.
     * @return un {@link Optional} con la entrega correspondiente, si existe.
     */
    Optional<Delivery> findByOrderId(Long orderId);
}
