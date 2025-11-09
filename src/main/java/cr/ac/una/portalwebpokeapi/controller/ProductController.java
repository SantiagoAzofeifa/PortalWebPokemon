package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.model.Category;
import cr.ac.una.portalwebpokeapi.model.Product;
import cr.ac.una.portalwebpokeapi.service.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;
    private final SessionManager sessions;

    public ProductController(ProductService service, SessionManager sessions) {
        this.service = service;
        this.sessions = sessions;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam Category category,
                                  @RequestParam(defaultValue="0") int page,
                                  @RequestParam(defaultValue="12") int size) {
        return ResponseEntity.ok(service.listByCategory(category, PageRequest.of(Math.max(0,page), Math.max(1,size))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("X-SESSION-TOKEN") String token, @RequestBody Product p) {
        requireAdmin(token);
        return ResponseEntity.ok(service.create(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestHeader("X-SESSION-TOKEN") String token, @PathVariable Long id, @RequestBody Product p) {
        requireAdmin(token);
        return ResponseEntity.ok(service.update(id, p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader("X-SESSION-TOKEN") String token, @PathVariable Long id) {
        requireAdmin(token);
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    private void requireAdmin(String token) {
        var me = sessions.get(token);
        if (me == null) throw new SecurityException("UNAUTHORIZED");
        if (!"ADMIN".equals(me.role)) throw new SecurityException("FORBIDDEN");
    }
}