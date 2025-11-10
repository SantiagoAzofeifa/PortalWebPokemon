package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link CartItem}.
 *
 * Proporciona métodos de acceso y manipulación de los ítems de un carrito.
 * Incluye consultas derivadas por convención para obtener o eliminar
 * los elementos asociados a un carrito específico.
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Obtiene todos los ítems asociados a un carrito dado.
     *
     * @param cartId ID del carrito.
     * @return lista de {@link CartItem} pertenecientes al carrito.
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * Elimina todos los ítems vinculados a un carrito.
     *
     * @param cartId ID del carrito del que se eliminarán los ítems.
     */
    void deleteByCartId(Long cartId);
}
