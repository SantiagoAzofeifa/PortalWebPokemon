package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.model.Category;
import cr.ac.una.portalwebpokeapi.model.Product;
import cr.ac.una.portalwebpokeapi.service.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para gestión de productos.
 * Permite listar, consultar y realizar CRUD con control de rol ADMIN.
 *
 * Base: /api/products
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;   // Lógica de negocio de productos
    private final SessionManager sessions;  // Control de autenticación y roles

    public ProductController(ProductService service, SessionManager sessions) {
        this.service = service;
        this.sessions = sessions;
    }

    /**
     * Lista productos por categoría con paginación.
     * GET /api/products?category=POKEMON&page=0&size=12
     *
     * @param category categoría requerida (POKEMON, ITEM, GAME, etc.)
     * @param page número de página (por defecto 0)
     * @param size cantidad por página (por defecto 12)
     * @return página de productos
     */
    @GetMapping
    public ResponseEntity<?> list(@RequestParam Category category,
                                  @RequestParam(defaultValue="0") int page,
                                  @RequestParam(defaultValue="12") int size) {
        return ResponseEntity.ok(
                service.list(category, PageRequest.of(Math.max(0, page), Math.max(1, size)))
        );
    }

    /**
     * Obtiene un producto por ID.
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id){
        return ResponseEntity.ok(service.get(id));
    }

    /**
     * Crea un nuevo producto (solo ADMIN).
     * POST /api/products
     *
     * @param token header X-SESSION-TOKEN
     * @param p body del producto a crear
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("X-SESSION-TOKEN") String token,
                                    @RequestBody Product p){
        requireAdmin(token);
        return ResponseEntity.ok(service.create(p));
    }

    /**
     * Actualiza un producto existente (solo ADMIN).
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestHeader("X-SESSION-TOKEN") String token,
                                    @PathVariable Long id,
                                    @RequestBody Product p){
        requireAdmin(token);
        return ResponseEntity.ok(service.update(id, p));
    }

    /**
     * Elimina un producto (solo ADMIN).
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader("X-SESSION-TOKEN") String token,
                                    @PathVariable Long id){
        requireAdmin(token);
        service.delete(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /**
     * Verifica que la sesión sea válida y el rol sea ADMIN.
     * Lanza SecurityException si no cumple, gestionada por RestExceptionHandler.
     */
    private void requireAdmin(String token){
        var me = sessions.get(token);
        if (me == null) throw new SecurityException("UNAUTHORIZED");
        if (!"ADMIN".equals(me.role)) throw new SecurityException("FORBIDDEN");
    }
}
