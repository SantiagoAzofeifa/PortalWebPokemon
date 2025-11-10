package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.PsRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link PsRecord}.
 *
 * Permite gestionar los registros del sistema postventa (PS), que documentan incidencias
 * o reclamos asociados a órdenes y productos. Proporciona consultas derivadas para
 * obtener los registros relacionados con una orden específica.
 */
public interface PsRecordRepository extends JpaRepository<PsRecord, Long> {

    /**
     * Obtiene todos los registros postventa asociados a una orden.
     *
     * @param orderId ID de la orden.
     * @return lista de {@link PsRecord} correspondientes a la orden.
     */
    List<PsRecord> findByOrderId(Long orderId);
}
