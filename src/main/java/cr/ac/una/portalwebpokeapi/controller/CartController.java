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
        this.cartService = cartService; this.sessions = sessions;
    }

    private Long requireUser(String token){
        var s = sessions.get(token);
        if (s==null) throw new SecurityException("UNAUTHORIZED");
        return Long.valueOf(s.userId);
    }

    @GetMapping
    public ResponseEntity<?> view(@RequestHeader("X-SESSION-TOKEN") String token){
        return ResponseEntity.ok(cartService.view(requireUser(token)));
    }

    public record AddCatalogReq(String category, String nameOrId, Integer quantity){}

    @PostMapping("/catalog")
    public ResponseEntity<?> addCatalog(@RequestHeader("X-SESSION-TOKEN") String token,
                                        @RequestBody AddCatalogReq req) {
        if (req.nameOrId()==null || req.nameOrId().isBlank())
            return ResponseEntity.badRequest().body(Map.of("error","nameOrId requerido"));
        cartService.addCatalog(requireUser(token),
                req.category()==null? "POKEMON" : req.category(),
                req.nameOrId(), req.quantity()==null?1:req.quantity());
        return ResponseEntity.ok(Map.of("ok",true));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<?> update(@RequestHeader("X-SESSION-TOKEN") String token,@PathVariable Long id,
                                    @RequestBody Map<String,Integer> body){
        int qty = Math.max(1, body.getOrDefault("quantity",1));
        cartService.updateQty(requireUser(token), id, qty);
        return ResponseEntity.ok(Map.of("ok",true));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> remove(@RequestHeader("X-SESSION-TOKEN") String token,@PathVariable Long id){
        cartService.removeItem(requireUser(token), id);
        return ResponseEntity.ok(Map.of("ok",true));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clear(@RequestHeader("X-SESSION-TOKEN") String token){
        cartService.clear(requireUser(token));
        return ResponseEntity.ok(Map.of("ok",true));
    }
}