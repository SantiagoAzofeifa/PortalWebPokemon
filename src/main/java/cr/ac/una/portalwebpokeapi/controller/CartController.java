package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final SessionManager sessions;

    public CartController(CartService cartService, SessionManager sessions) {
        this.cartService = cartService;
        this.sessions = sessions;
    }

    private Long requireUserId(String token) {
        var s = sessions.get(token);
        if (s == null) throw new SecurityException("UNAUTHORIZED");
        return Long.valueOf(s.userId);
    }

    @GetMapping
    public ResponseEntity<?> getCart(@RequestHeader("X-SESSION-TOKEN") String token) {
        return ResponseEntity.ok(cartService.getCartResponse(requireUserId(token)));
    }

    public record AddItemReq(Long productId, Integer quantity) {}

    @PostMapping("/items")
    public ResponseEntity<?> addItem(@RequestHeader("X-SESSION-TOKEN") String token,
                                     @RequestBody AddItemReq req) {
        if (req.productId() == null) return ResponseEntity.badRequest().body(Map.of("error","productId requerido"));
        int qty = req.quantity() == null ? 1 : req.quantity();
        cartService.addItem(requireUserId(token), req.productId(), qty);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateItem(@RequestHeader("X-SESSION-TOKEN") String token,
                                        @PathVariable Long itemId,
                                        @RequestBody Map<String,Integer> body) {
        int qty = Math.max(1, body.getOrDefault("quantity",1));
        cartService.updateItemQuantity(requireUserId(token), itemId, qty);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> deleteItem(@RequestHeader("X-SESSION-TOKEN") String token,
                                        @PathVariable Long itemId) {
        cartService.removeItem(requireUserId(token), itemId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clear(@RequestHeader("X-SESSION-TOKEN") String token) {
        cartService.clearCart(requireUserId(token));
        return ResponseEntity.ok(Map.of("ok", true));
    }
}