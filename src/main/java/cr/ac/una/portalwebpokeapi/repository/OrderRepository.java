package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Order}.
 *
 * Gestiona las operaciones CRUD sobre las órdenes de compra y
 * proporciona métodos personalizados para consultar las órdenes
 * asociadas a un usuario específico.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Obtiene todas las órdenes creadas por un usuario determinado.
     *
     * @param userId ID del usuario.
     * @return lista de órdenes asociadas al usuario.
     */
    List<Order> findByUserId(Long userId);
}
