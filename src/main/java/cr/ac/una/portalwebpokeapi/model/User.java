package cr.ac.una.portalwebpokeapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Entidad que representa a un usuario dentro del sistema.
 *
 * Contiene credenciales, rol, estado y fecha de creación.
 * El campo passwordHash almacena la contraseña en formato cifrado (BCrypt).
 */
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    /** Identificador único del usuario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de usuario único utilizado para autenticación. */
    @Column(nullable = false, unique = true, length = 80)
    private String username;

    /** Hash de la contraseña (cifrado con BCrypt). */
    @Column(nullable = false, length = 120)
    private String passwordHash;

    /** Rol asignado al usuario: ADMIN o USER. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    /** Estado de la cuenta (true = activa, false = deshabilitada). */
    @Column(nullable = false)
    private boolean active = true;

    /** Código de país ISO-2 del usuario (ej: CR, US, MX). */
    @Column(name = "country_code", length = 2)
    private String countryCode;

    /** Fecha y hora de creación del usuario (UTC). */
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}