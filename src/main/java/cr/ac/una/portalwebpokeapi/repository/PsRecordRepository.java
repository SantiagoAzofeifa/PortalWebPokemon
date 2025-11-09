package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.PsRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PsRecordRepository extends JpaRepository<PsRecord,Long> {
    List<PsRecord> findByOrderId(Long orderId);
}