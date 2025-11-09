package cr.ac.una.portalwebpokeapi.repository;

import cr.ac.una.portalwebpokeapi.model.Category;
import cr.ac.una.portalwebpokeapi.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad {@link Product}.
 *
 * Administra las operaciones CRUD sobre los productos del catálogo
 * y proporciona métodos personalizados para filtrar por categoría
 * con paginación incluida.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Obtiene una página de productos filtrados por categoría.
     *
     * @param category categoría del producto (POKEMON, ITEMS, GENERATIONS, etc.)
     * @param pageable objeto de paginación (número de página y tamaño).
     * @return una {@link Page} de productos pertenecientes a la categoría especificada.
     */
    Page<Product> findByCategory(Category category, Pageable pageable);
}
