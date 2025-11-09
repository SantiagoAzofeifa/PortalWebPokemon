package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link OrderItem}.
 *
 * Permite realizar operaciones CRUD sobre los ítems de una orden.
 * Proporciona consultas derivadas para listar o eliminar los ítems
 * asociados a una orden específica.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Obtiene todos los ítems pertenecientes a una orden.
     *
     * @param orderId ID de la orden.
     * @return lista de {@link OrderItem} asociados.
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Elimina todos los ítems asociados a una orden.
     *
     * @param orderId ID de la orden.
     */
    void deleteByOrderId(Long orderId);
}
