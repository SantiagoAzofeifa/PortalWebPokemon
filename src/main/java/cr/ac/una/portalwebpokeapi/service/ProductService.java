package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.Category;
import cr.ac.una.portalwebpokeapi.model.Product;
import cr.ac.una.portalwebpokeapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Servicio de productos.
 *
 * Responsabilidades:
 * - Listar productos por categoría con paginación.
 * - CRUD básico: obtener, crear, actualizar parcial y eliminar.
 * - Validación mínima de dominio antes de persistir.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    /** Repositorio JPA de productos. */
    private final ProductRepository repo;

    /**
     * Lista productos de una categoría con paginación.
     *
     * @param category categoría requerida (enum)
     * @param pageable parámetros de paginación
     * @return página de productos
     */
    public Page<Product> list(Category category, Pageable pageable) {
        return repo.findByCategory(category, pageable);
    }

    /**
     * Obtiene un producto por ID.
     *
     * @param id identificador del producto
     * @return producto existente
     * @throws IllegalArgumentException si no existe
     */
    public Product get(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    /**
     * Crea un producto nuevo.
     * Fuerza id=null para evitar updates accidentales.
     *
     * @param p producto a crear
     * @return producto persistido
     */
    public Product create(Product p) {
        validate(p);
        p.setId(null);
        return repo.save(p);
    }

    /**
     * Actualiza parcialmente un producto existente.
     * Solo copia campos no nulos desde el payload.
     *
     * @param id id del producto a actualizar
     * @param p  datos a aplicar
     * @return producto actualizado
     */
    public Product update(Long id, Product p) {
        Product cur = get(id); // asegura existencia

        // Patch por campos no nulos
        if (p.getName() != null) cur.setName(p.getName());
        if (p.getCategory() != null) cur.setCategory(p.getCategory());
        if (p.getImageUrl() != null) cur.setImageUrl(p.getImageUrl());
        if (p.getPrice() != null) cur.setPrice(p.getPrice());
        if (p.getCountryOfOrigin() != null) cur.setCountryOfOrigin(p.getCountryOfOrigin());
        if (p.getAvailableCountriesCsv() != null) cur.setAvailableCountriesCsv(p.getAvailableCountriesCsv());
        if (p.getBannedCountriesCsv() != null) cur.setBannedCountriesCsv(p.getBannedCountriesCsv());
        if (p.getDescription() != null) cur.setDescription(p.getDescription());

        validate(cur);
        return repo.save(cur);
    }

    /**
     * Elimina un producto por ID.
     *
     * @param id identificador del producto
     * @throws IllegalArgumentException si no existe
     */
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("Producto no encontrado");
        repo.deleteById(id);
    }

    /**
     * Validación mínima de dominio.
     * - category obligatorio
     * - name obligatorio no vacío
     * - price no nulo y >= 0
     *
     * @param p producto a validar
     */
    private void validate(Product p) {
        if (p.getCategory() == null) throw new IllegalArgumentException("Categoría requerida");
        if (p.getName() == null || p.getName().isBlank()) throw new IllegalArgumentException("Nombre requerido");
        if (p.getPrice() == null || p.getPrice() < 0) throw new IllegalArgumentException("Precio inválido");
    }
}
