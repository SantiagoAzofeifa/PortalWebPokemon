package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.model.Cart;
import cr.ac.una.portalwebpokeapi.model.CartItem;
import cr.ac.una.portalwebpokeapi.repository.CartItemRepository;
import cr.ac.una.portalwebpokeapi.repository.CartRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartRepository carts;
    private final CartItemRepository items;
    private final SessionManager sessions;

    public CartController(CartRepository carts, CartItemRepository items, SessionManager sessions) {
        this.carts = carts;
        this.items = items;
        this.sessions = sessions;
    }

    private Long requireUserId(String token) {
        var s = sessions.get(token);
        if (s == null) throw new RuntimeException("UNAUTHORIZED");
        return Long.valueOf(s.userId);
    }

    @GetMapping
    public ResponseEntity<?> getCart(@RequestHeader("X-SESSION-TOKEN") String token) {
        Long userId = requireUserId(token);
        var cart = carts.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            return carts.save(c);
        });
        List<CartItem> list = items.findByCartId(cart.getId());
        return ResponseEntity.ok(Map.of("cartId", cart.getId(), "items", list));
    }

    public record AddItemReq(Long productId, int quantity, double unitPrice) {}

    @PostMapping("/items")
    public ResponseEntity<?> addItem(@RequestHeader("X-SESSION-TOKEN") String token, @RequestBody AddItemReq req) {
        Long userId = requireUserId(token);
        var cart = carts.findByUserId(userId).orElseThrow();
        CartItem it = new CartItem();
        it.setCartId(cart.getId());
        it.setProductId(req.productId());
        it.setQuantity(Math.max(1, req.quantity()));
        it.setUnitPrice(req.unitPrice());
        items.save(it);
        return ResponseEntity.ok(Map.of("ok",true));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateItem(@RequestHeader("X-SESSION-TOKEN") String token, @PathVariable Long itemId,
                                        @RequestBody Map<String, Integer> body) {
        requireUserId(token);
        var it = items.findById(itemId).orElseThrow();
        it.setQuantity(Math.max(1, body.getOrDefault("quantity", it.getQuantity())));
        items.save(it);
        return ResponseEntity.ok(Map.of("ok",true));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> deleteItem(@RequestHeader("X-SESSION-TOKEN") String token, @PathVariable Long itemId) {
        requireUserId(token);
        items.deleteById(itemId);
        return ResponseEntity.ok(Map.of("ok",true));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clear(@RequestHeader("X-SESSION-TOKEN") String token) {
        Long userId = requireUserId(token);
        var cart = carts.findByUserId(userId).orElseThrow();
        items.deleteByCartId(cart.getId());
        return ResponseEntity.ok(Map.of("ok",true));
    }
}