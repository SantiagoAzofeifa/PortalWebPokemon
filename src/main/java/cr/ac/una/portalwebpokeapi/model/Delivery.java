package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Entidad que representa la información de entrega (delivery) asociada a una orden.
 * Contiene los datos sobre el método de envío, dirección, fecha programada, código
 * de rastreo y notas del pedido. Se relaciona de forma 1:1 con la entidad Order.
 */
@Getter
@Entity
@Table(name = "delivery")
public class Delivery {

    /** Identificador único de la entrega (clave primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID de la orden a la cual pertenece esta entrega. Es único y obligatorio. */
    @Setter
    @Column(nullable = false, unique = true)
    private Long orderId;

    /** Método de entrega (ej. CORREO, MENSAJERO, CASILLERO). */
    @Setter
    @Column(length = 30)
    private String method;

    /** Dirección de entrega completa. */
    @Setter
    @Column(length = 300)
    private String address;

    /** Fecha y hora programada para la entrega (en formato UTC). */
    @Setter
    private Instant scheduledDate;

    /** Código de seguimiento proporcionado por el transportista. */
    @Setter
    @Column(length = 80)
    private String trackingCode;

    /** Campo opcional para notas adicionales del cliente o del transportista. */
    @Setter
    @Column(length = 500)
    private String notes;
}
