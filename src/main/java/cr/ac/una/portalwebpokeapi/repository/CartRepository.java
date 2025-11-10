package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Cart}.
 *
 * Permite realizar operaciones CRUD sobre los carritos de compra,
 * así como buscar el carrito activo asociado a un usuario específico.
 */
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Busca el carrito asociado a un usuario.
     *
     * @param userId ID del usuario propietario del carrito.
     * @return un {@link Optional} con el carrito si existe, vacío en caso contrario.
     */
    Optional<Cart> findByUserId(Long userId);
}
