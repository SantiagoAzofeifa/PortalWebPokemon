package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link User}.
 *
 * Gestiona la persistencia y consultas relacionadas con los usuarios del sistema,
 * incluyendo autenticación, validación de existencia y recuperación por nombre de usuario.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su nombre de usuario (username).
     *
     * @param username nombre de usuario.
     * @return un {@link Optional} con el usuario si existe.
     */
    Optional<User> findByUsername(String username);

    /**
     * Verifica si ya existe un usuario con el nombre de usuario especificado.
     *
     * @param username nombre de usuario a validar.
     * @return true si existe, false en caso contrario.
     */
    boolean existsByUsername(String username);
}
