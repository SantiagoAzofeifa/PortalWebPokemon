package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa un ítem perteneciente a una orden.
 *
 * Cada OrderItem almacena una copia del producto vendido, su categoría,
 * cantidad y precio unitario al momento de la compra. Esto garantiza que
 * el historial de precios y artículos quede registrado incluso si el
 * catálogo cambia posteriormente.
 */
@Getter
@Setter
@Entity
@Table(name = "order_items")
public class OrderItem {

    /** Identificador único del ítem en la orden. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID de la orden a la que pertenece este ítem (relación 1:N con Order). */
    @Column(nullable = false)
    private Long orderId;

    /** ID del producto asociado (Pokémon, ítem o versión de juego). */
    @Column(nullable = false)
    private Long productId;

    /** Categoría del producto: POKEMON | ITEM | GAME. */
    @Column(nullable = false, length = 20)
    private String productCategory = "POKEMON";

    /** Cantidad de unidades de este producto en la orden. */
    @Column(nullable = false)
    private int quantity;

    /** Precio unitario del producto al momento de la compra. */
    @Column(nullable = false)
    private double unitPrice;
}
