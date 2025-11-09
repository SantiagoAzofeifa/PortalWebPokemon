package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Entidad que representa un registro del sistema de postventa (PS).
 *
 * Un PsRecord documenta incidencias, reclamos o revisiones asociadas a una orden
 * y un producto específico. Permite rastrear motivos, estado de resolución y fecha
 * de creación del registro.
 */
@Getter
@Setter
@Entity
@Table(name = "ps_records")
public class PsRecord {

    /** Identificador único del registro postventa. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID de la orden asociada al reclamo o revisión. */
    @Column(nullable = false)
    private Long orderId;

    /** ID del producto afectado dentro de la orden. */
    @Column(nullable = false)
    private Long productId;

    /** Motivo del reclamo o registro postventa. */
    @Column(length = 200)
    private String reason;

    /** Indica si el caso ha sido resuelto (true/false). */
    private Boolean resolved = false;

    /** Fecha y hora de creación del registro (UTC). */
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
