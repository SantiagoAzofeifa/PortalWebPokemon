package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.dto.CheckoutRequest;
import cr.ac.una.portalwebpokeapi.model.Delivery;
import cr.ac.una.portalwebpokeapi.model.Order;
import cr.ac.una.portalwebpokeapi.model.Packaging;
import cr.ac.una.portalwebpokeapi.model.Payment;
import cr.ac.una.portalwebpokeapi.model.Warehouse;
import cr.ac.una.portalwebpokeapi.service.OrderFlowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderFlowController {

    private final OrderFlowService flow;
    private final SessionManager sessions;

    public OrderFlowController(OrderFlowService flow, SessionManager sessions) {
        this.flow = flow;
        this.sessions = sessions;
    }

    private Long requireUser(String token){
        var s = sessions.get(token);
        if (s==null) throw new SecurityException("UNAUTHORIZED");
        return Long.valueOf(s.userId);
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestHeader("X-SESSION-TOKEN") String token,
                                      @RequestBody CheckoutRequest req){
        Long userId = requireUser(token);
        Order order = new Order();
        order.setCustomerName(req.getCustomerName());
        order.setCustomerEmail(req.getCustomerEmail());
        order.setCustomerPhone(req.getCustomerPhone());
        order.setAddressLine1(req.getAddressLine1());
        order.setAddressLine2(req.getAddressLine2());
        order.setCountry(req.getCountry());
        order.setRegion(req.getRegion());
        order.setStatus("CREATED");
        Order saved = flow.checkoutFromCart(userId, order);
        return ResponseEntity.ok(Map.of("orderId", saved.getId()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> view(@RequestHeader("X-SESSION-TOKEN") String token,
                                  @PathVariable Long orderId){
        requireUser(token);
        return ResponseEntity.ok(flow.viewOrder(orderId));
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mine(@RequestHeader("X-SESSION-TOKEN") String token){
        Long userId = requireUser(token);
        return ResponseEntity.ok(flow.listForUser(userId));
    }

    @PutMapping("/{orderId}/warehouse")
    public ResponseEntity<?> warehouse(@RequestHeader("X-SESSION-TOKEN") String token,
                                       @PathVariable Long orderId,
                                       @RequestBody Warehouse w){
        requireUser(token);
        return ResponseEntity.ok(flow.upsertWarehouse(orderId, w));
    }

    @PutMapping("/{orderId}/packaging")
    public ResponseEntity<?> packaging(@RequestHeader("X-SESSION-TOKEN") String token,
                                       @PathVariable Long orderId,
                                       @RequestBody Packaging p){
        requireUser(token);
        return ResponseEntity.ok(flow.upsertPackaging(orderId, p));
    }

    @PutMapping("/{orderId}/delivery")
    public ResponseEntity<?> delivery(@RequestHeader("X-SESSION-TOKEN") String token,
                                      @PathVariable Long orderId,
                                      @RequestBody Delivery d){
        requireUser(token);
        return ResponseEntity.ok(flow.upsertDelivery(orderId, d));
    }

    @PutMapping("/{orderId}/payment")
    public ResponseEntity<?> payment(@RequestHeader("X-SESSION-TOKEN") String token,
                                     @PathVariable Long orderId,
                                     @RequestBody Payment p){
        requireUser(token);
        return ResponseEntity.ok(flow.upsertPayment(orderId, p));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@RequestHeader("X-SESSION-TOKEN") String token,
                                         @PathVariable Long orderId){
        requireUser(token);
        flow.deleteOrderCascade(orderId);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}