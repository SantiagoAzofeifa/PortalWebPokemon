package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa un ítem dentro del carrito de compras.
 *
 * Cada CartItem corresponde a un producto específico agregado por el usuario,
 * identificado por su categoría y productId. El precio unitario se almacena
 * en el momento de la inserción para evitar inconsistencias si el catálogo cambia.
 */
@Getter
@Setter
@Entity
@Table(name = "cart_items")
public class CartItem {

    /** Identificador único del ítem dentro del carrito. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del carrito al que pertenece este ítem (relación 1:N con Cart). */
    @Column(nullable = false)
    private Long cartId;

    /** ID del producto asociado (ej. Pokémon, ítem o versión de juego). */
    @Column(nullable = false)
    private Long productId;

    /** Categoría del producto: POKEMON | ITEM | GAME. */
    @Column(nullable = false, length = 20)
    private String productCategory = "POKEMON";

    /** Cantidad del producto seleccionada por el usuario. */
    @Column(nullable = false)
    private int quantity;

    /** Precio unitario del producto al momento de agregarse al carrito. */
    @Column(nullable = false)
    private double unitPrice;
}
