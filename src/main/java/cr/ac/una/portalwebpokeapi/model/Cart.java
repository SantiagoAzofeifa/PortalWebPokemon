package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa el carrito de compras asociado a un usuario.
 *
 * Cada usuario tiene un único carrito activo identificado por su userId.
 * Los ítems del carrito se modelan en una tabla separada (relación 1:N con CartItem).
 */
@Setter
@Getter
@Entity
@Table(name = "carts")
public class Cart {

    /** Identificador único del carrito (clave primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del usuario propietario del carrito. Es único y obligatorio. */
    @Column(nullable = false, unique = true)
    private Long userId;
}
