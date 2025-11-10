package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.Packaging;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Packaging}.
 *
 * Permite realizar operaciones CRUD sobre los registros de empaquetado
 * y consultar la información asociada a una orden específica.
 */
public interface PackagingRepository extends JpaRepository<Packaging, Long> {

    /**
     * Busca el registro de empaquetado vinculado a una orden.
     *
     * @param orderId ID de la orden.
     * @return un {@link Optional} con el empaquetado correspondiente, si existe.
     */
    Optional<Packaging> findByOrderId(Long orderId);
}
