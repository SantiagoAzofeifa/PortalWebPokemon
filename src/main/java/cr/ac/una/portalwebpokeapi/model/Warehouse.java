package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Entidad que representa el registro de control de bodega asociado a una orden.
 *
 * Incluye información sobre entrada, salida, control de inventario, ubicación,
 * país de origen y observaciones relacionadas con el almacenamiento o despacho.
 * Relación 1:1 con la entidad Order.
 */
@Getter
@Setter
@Entity
@Table(name = "warehouse")
public class Warehouse {

    /** Identificador único del registro de bodega. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID de la orden asociada (relación 1:1). */
    @Column(nullable = false, unique = true)
    private Long orderId;

    /** Fecha y hora de ingreso del pedido al almacén. */
    private Instant inDate;

    /** Fecha y hora de salida del pedido del almacén. */
    private Instant outDate;

    /** Indica si el stock fue verificado antes de la salida. */
    private Boolean stockChecked;

    /** Cantidad de unidades verificadas en inventario. */
    private Integer stockQty;

    /** Ubicación física o pasillo dentro del almacén. */
    @Column(length = 120)
    private String location;

    /** País de origen del envío o bodega. */
    @Column(length = 120)
    private String originCountry;

    /** Notas o comentarios adicionales sobre la gestión en almacén. */
    @Column(length = 500)
    private String notes;
}
