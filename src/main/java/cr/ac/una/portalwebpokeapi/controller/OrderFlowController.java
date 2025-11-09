package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.model.*;
import cr.ac.una.portalwebpokeapi.service.OrderFlowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController @RequestMapping("/api/orders")
public class OrderFlowController {
    private final OrderFlowService flow;
    private final SessionManager sessions;

    public OrderFlowController(OrderFlowService flow, SessionManager sessions) {
        this.flow = flow; this.sessions = sessions;
    }

    private Long requireUser(String token){
        var s = sessions.get(token);
        if (s==null) throw new SecurityException("UNAUTHORIZED");
        return Long.valueOf(s.userId);
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestHeader("X-SESSION-TOKEN") String token, @RequestBody Order order){
        Long userId = requireUser(token);
        Order o = flow.checkoutFromCart(userId, order);
        return ResponseEntity.ok(Map.of("orderId", o.getId()));
    }

    // ... resto de endpoints (etapas y P-S) ...
}