package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API del carrito de compras.
 * Requiere header X-SESSION-TOKEN. Gestiona ver, agregar, actualizar, eliminar y limpiar.
 * Base: /api/cart
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;   // Lógica de negocio del carrito
    private final SessionManager sessions;   // Valida token y obtiene userId

    public CartController(CartService cartService, SessionManager sessions) {
        this.cartService = cartService;
        this.sessions = sessions;
    }

    /**
     * Verifica sesión y devuelve el userId como Long.
     * Lanza SecurityException("UNAUTHORIZED") si no hay sesión.
     */
    private Long requireUser(String token){
        var s = sessions.get(token);
        if (s == null) throw new SecurityException("UNAUTHORIZED");
        return Long.valueOf(s.userId);
    }

    /**
     * Devuelve el estado actual del carrito del usuario.
     * GET /api/cart
     * @param token X-SESSION-TOKEN
     * @return 200 con DTO del carrito.
     */
    @GetMapping
    public ResponseEntity<?> view(@RequestHeader("X-SESSION-TOKEN") String token){
        return ResponseEntity.ok(cartService.view(requireUser(token)));
    }

    /**
     * Payload para agregar ítems desde catálogo.
     * category opcional. nameOrId requerido. quantity opcional.
     */
    public record AddCatalogReq(String category, String nameOrId, Integer quantity){}

    /**
     * Agrega un ítem al carrito resolviendo por categoría y nombre/ID.
     * POST /api/cart/catalog
     * Body: {"category":"POKEMON|ITEM|...","nameOrId":"pikachu|25", "quantity":1}
     * category por defecto "POKEMON". quantity por defecto 1.
     * @return 200 {"ok":true} o 400 si falta nameOrId.
     */
    @PostMapping("/catalog")
    public ResponseEntity<?> addCatalog(@RequestHeader("X-SESSION-TOKEN") String token,
                                        @RequestBody AddCatalogReq req) {
        if (req.nameOrId() == null || req.nameOrId().isBlank())
            return ResponseEntity.badRequest().body(Map.of("error","nameOrId requerido"));

        cartService.addCatalog(
                requireUser(token),
                req.category() == null ? "POKEMON" : req.category(),
                req.nameOrId(),
                req.quantity() == null ? 1 : req.quantity()
        );
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /**
     * Actualiza la cantidad de una línea del carrito.
     * PUT /api/cart/items/{id}
     * Body: {"quantity": <int>=1} ; mínimo 1.
     * @return 200 {"ok":true}
     */
    @PutMapping("/items/{id}")
    public ResponseEntity<?> update(@RequestHeader("X-SESSION-TOKEN") String token,
                                    @PathVariable Long id,
                                    @RequestBody Map<String,Integer> body){
        int qty = Math.max(1, body.getOrDefault("quantity", 1));
        cartService.updateQty(requireUser(token), id, qty);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /**
     * Elimina una línea del carrito por ID.
     * DELETE /api/cart/items/{id}
     * @return 200 {"ok":true}
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> remove(@RequestHeader("X-SESSION-TOKEN") String token,
                                    @PathVariable Long id){
        cartService.removeItem(requireUser(token), id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /**
     * Limpia por completo el carrito del usuario.
     * DELETE /api/cart/clear
     * @return 200 {"ok":true}
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clear(@RequestHeader("X-SESSION-TOKEN") String token){
        cartService.clear(requireUser(token));
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
