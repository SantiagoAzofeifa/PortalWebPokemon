package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Entidad que representa una orden o pedido generado por un usuario.
 *
 * Contiene los datos básicos de identificación del pedido, cliente, dirección
 * y estado. Las relaciones con otras entidades (Delivery, Payment, etc.)
 * se gestionan desde el flujo de negocio correspondiente.
 */
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    /** Identificador único de la orden. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del usuario que realizó la compra. */
    @Column(nullable = false)
    private Long userId;

    /** Fecha y hora en que se creó la orden (UTC). */
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    /** Nombre del cliente asociado a la orden. */
    @Column(length = 120)
    private String customerName;

    /** Correo electrónico del cliente. */
    @Column(length = 120)
    private String customerEmail;

    /** Número telefónico del cliente. */
    @Column(length = 40)
    private String customerPhone;

    /** Primera línea de dirección (calle, número, etc.). */
    @Column(length = 200)
    private String addressLine1;

    /** Segunda línea de dirección (complemento opcional). */
    @Column(length = 200)
    private String addressLine2;

    /** País de destino de la entrega. */
    @Column(length = 120)
    private String country;

    /** Región, provincia o estado dentro del país. */
    @Column(length = 120)
    private String region;

    /** Estado actual de la orden (ej. CREATED, PAID, SHIPPED, DELIVERED). */
    @Column(length = 30, nullable = false)
    private String status = "CREATED";
}
