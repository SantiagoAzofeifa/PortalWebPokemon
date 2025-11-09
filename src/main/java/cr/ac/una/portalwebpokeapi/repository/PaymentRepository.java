package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Payment}.
 *
 * Administra los registros de pago asociados a las órdenes de compra.
 * Proporciona un método de consulta para obtener el pago correspondiente
 * a una orden específica.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Busca el pago asociado a una orden determinada.
     *
     * @param orderId ID de la orden.
     * @return un {@link Optional} con la información del pago si existe.
     */
    Optional<Payment> findByOrderId(Long orderId);
}
