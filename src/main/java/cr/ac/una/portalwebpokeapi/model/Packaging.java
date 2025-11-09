package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa la información del empaquetado de una orden.
 * Define el tipo, tamaño, materiales y condición del paquete (frágil o no).
 * Se relaciona de forma 1:1 con la entidad Order.
 */
@Getter
@Setter
@Entity
@Table(name = "packaging")
public class Packaging {

    /** Identificador único del registro de empaquetado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID de la orden asociada (relación 1:1). */
    @Column(nullable = false, unique = true)
    private Long orderId;

    /** Tamaño del paquete: GRANDE, MEDIANO o PEQUEÑO. */
    @Column(length = 20)
    private String size;

    /** Tipo de empaquetado: LIBRE, AL_VACIO o RELLENO. */
    @Column(length = 20)
    private String type;

    /** Materiales usados para el embalaje. */
    @Column(length = 120)
    private String materials;

    /** Indica si el contenido es frágil (true/false). */
    private Boolean fragile;

    /** Notas adicionales o comentarios sobre el empaquetado. */
    @Column(length = 500)
    private String notes;
}
