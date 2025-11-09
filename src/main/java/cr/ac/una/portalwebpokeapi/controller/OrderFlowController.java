package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.model.*;
import cr.ac.una.portalwebpokeapi.service.OrderFlowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    public record CreateOrderReq(Order order, List<Long> productIds){}

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("X-SESSION-TOKEN") String token, @RequestBody CreateOrderReq req){
        Long userId = requireUser(token);
        Order o = flow.createOrder(userId, req.order(), req.productIds()==null? List.of(): req.productIds());
        return ResponseEntity.ok(Map.of("orderId", o.getId()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> view(@RequestHeader("X-SESSION-TOKEN") String token, @PathVariable Long orderId){
        requireUser(token);
        return ResponseEntity.ok(flow.viewOrder(orderId));
    }

    @PutMapping("/{orderId}/warehouse")
    public ResponseEntity<?> warehouse(@RequestHeader("X-SESSION-TOKEN") String token,@PathVariable Long orderId,@RequestBody Warehouse w){
        requireUser(token);
        return ResponseEntity.ok(flow.upsertWarehouse(orderId,w));
    }

    @PutMapping("/{orderId}/packaging")
    public ResponseEntity<?> packaging(@RequestHeader("X-SESSION-TOKEN") String token,@PathVariable Long orderId,@RequestBody Packaging p){
        requireUser(token);
        return ResponseEntity.ok(flow.upsertPackaging(orderId,p));
    }

    @PutMapping("/{orderId}/delivery")
    public ResponseEntity<?> delivery(@RequestHeader("X-SESSION-TOKEN") String token,@PathVariable Long orderId,@RequestBody Delivery d){
        requireUser(token);
        return ResponseEntity.ok(flow.upsertDelivery(orderId,d));
    }

    @PutMapping("/{orderId}/payment")
    public ResponseEntity<?> payment(@RequestHeader("X-SESSION-TOKEN") String token,@PathVariable Long orderId,@RequestBody Payment p){
        requireUser(token);
        return ResponseEntity.ok(flow.upsertPayment(orderId,p));
    }

    @PostMapping("/{orderId}/ps")
    public ResponseEntity<?> createPs(@RequestHeader("X-SESSION-TOKEN") String token,@PathVariable Long orderId,@RequestBody PsRecord ps){
        requireUser(token);
        return ResponseEntity.ok(flow.createPsRecord(orderId, ps));
    }

    @PutMapping("/ps/{recordId}/resolve")
    public ResponseEntity<?> resolvePs(@RequestHeader("X-SESSION-TOKEN") String token,@PathVariable Long recordId){
        requireUser(token);
        return ResponseEntity.ok(flow.resolvePs(recordId));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@RequestHeader("X-SESSION-TOKEN") String token,@PathVariable Long orderId){
        requireUser(token);
        flow.deleteOrderCascade(orderId);
        return ResponseEntity.ok(Map.of("ok",true));
    }
}