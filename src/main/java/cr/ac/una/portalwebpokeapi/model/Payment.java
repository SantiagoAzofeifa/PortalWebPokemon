package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Entidad que almacena la información del pago asociado a una orden.
 * Registra el método, montos, moneda y fecha del pago, además de notas opcionales.
 * Existe una relación 1:1 con la entidad Order.
 */
@Getter
@Setter
@Entity
@Table(name = "payment")
public class Payment {

    /** Identificador único del registro de pago. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID de la orden asociada (relación 1:1). */
    @Column(nullable = false, unique = true)
    private Long orderId;

    /** Moneda utilizada en la transacción (por ejemplo, USD, CRC, EUR). */
    @Column(length = 10)
    private String currency;

    /** Número de ítems incluidos en la orden. */
    private Integer itemCount;

    /** Monto bruto total del pedido (sin descuentos ni impuestos aplicados). */
    private Double grossAmount;

    /** Monto neto final a pagar (después de ajustes, descuentos o impuestos). */
    private Double netAmount;

    /** Método de pago utilizado (ej. Tarjeta, Transferencia, Efectivo, etc.). */
    @Column(length = 30)
    private String method;

    /** Fecha y hora en que se registró el pago (UTC). */
    private Instant paidAt;

    /** Campo opcional para observaciones o referencias adicionales. */
    @Column(length = 500)
    private String notes;
}
