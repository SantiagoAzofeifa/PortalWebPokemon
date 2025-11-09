package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Warehouse}.
 *
 * Gestiona los registros de control de bodega asociados a las órdenes.
 * Permite realizar operaciones CRUD y consultar la información de
 * almacenamiento o despacho vinculada a una orden específica.
 */
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    /**
     * Busca el registro de bodega asociado a una orden.
     *
     * @param orderId ID de la orden.
     * @return un {@link Optional} con el registro de bodega si existe.
     */
    Optional<Warehouse> findByOrderId(Long orderId);
}
