package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.dto.CheckoutRequest;
import cr.ac.una.portalwebpokeapi.model.Delivery;
import cr.ac.una.portalwebpokeapi.model.Order;
import cr.ac.una.portalwebpokeapi.model.Packaging;
import cr.ac.una.portalwebpokeapi.model.Payment;
import cr.ac.una.portalwebpokeapi.model.PsRecord;
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
        System.out.println("[C-SEC] requireUser() token=" + (token==null?"<null>":(token.length()>12? token.substring(0,12)+"...":token)));
        var s = sessions.get(token);
        if (s==null) {
            System.out.println("[C-SEC] Sesión NO válida -> UNAUTHORIZED");
            throw new SecurityException("UNAUTHORIZED");
        }
        System.out.println("[C-SEC] Sesión OK userId=" + s.userId + " username=" + s.username + " role=" + s.role);
        return Long.valueOf(s.userId);
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestHeader("X-SESSION-TOKEN") String token,
                                      @RequestBody CheckoutRequest req){
        System.out.println("===== [C-CHK] POST /api/orders/checkout =====");
        Long userId = requireUser(token);
        System.out.println("[C-CHK] Payload -> name=" + req.getCustomerName()
                + " email=" + req.getCustomerEmail()
                + " phone=" + req.getCustomerPhone()
                + " addr1=" + req.getAddressLine1()
                + " addr2=" + req.getAddressLine2()
                + " country=" + req.getCountry()
                + " region=" + req.getRegion());
        try {
            Order order = new Order();
            order.setCustomerName(req.getCustomerName());
            order.setCustomerEmail(req.getCustomerEmail());
            order.setCustomerPhone(req.getCustomerPhone());
            order.setAddressLine1(req.getAddressLine1());
            order.setAddressLine2(req.getAddressLine2());
            order.setCountry(req.getCountry());
            order.setRegion(req.getRegion());
            order.setStatus("CREATED");
            var saved = flow.checkoutFromCart(userId, order);
            System.out.println("[C-CHK] OK orderId=" + saved.getId());
            return ResponseEntity.ok(Map.of("orderId", saved.getId()));
        } catch (Exception ex) {
            System.out.println("[C-CHK] EXCEPTION en checkout: " + ex.getClass().getName() + " -> " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> view(@RequestHeader("X-SESSION-TOKEN") String token,
                                  @PathVariable Long orderId){
        System.out.println("===== [C-VIEW] GET /api/orders/" + orderId + " =====");
        requireUser(token);
        try {
            var out = flow.viewOrder(orderId);
            System.out.println("[C-VIEW] OK keys=" + out.keySet());
            return ResponseEntity.ok(out);
        } catch (Exception ex) {
            System.out.println("[C-VIEW] EXCEPTION: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mine(@RequestHeader("X-SESSION-TOKEN") String token){
        System.out.println("===== [C-MINE] GET /api/orders/mine =====");
        Long userId = requireUser(token);
        try {
            var list = flow.listForUser(userId);
            System.out.println("[C-MINE] OK total=" + list.size());
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            System.out.println("[C-MINE] EXCEPTION: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @PutMapping("/{orderId}/warehouse")
    public ResponseEntity<?> warehouse(@RequestHeader("X-SESSION-TOKEN") String token,
                                       @PathVariable Long orderId,
                                       @RequestBody Warehouse w){
        System.out.println("===== [C-WH] PUT /api/orders/"+orderId+"/warehouse =====");
        requireUser(token);
        try {
            var saved = flow.upsertWarehouse(orderId, w);
            System.out.println("[C-WH] OK savedId=" + saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            System.out.println("[C-WH] EXCEPTION: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @PutMapping("/{orderId}/packaging")
    public ResponseEntity<?> packaging(@RequestHeader("X-SESSION-TOKEN") String token,
                                       @PathVariable Long orderId,
                                       @RequestBody Packaging p){
        System.out.println("===== [C-PK] PUT /api/orders/"+orderId+"/packaging =====");
        requireUser(token);
        try {
            var saved = flow.upsertPackaging(orderId, p);
            System.out.println("[C-PK] OK savedId=" + saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            System.out.println("[C-PK] EXCEPTION: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @PutMapping("/{orderId}/delivery")
    public ResponseEntity<?> delivery(@RequestHeader("X-SESSION-TOKEN") String token,
                                      @PathVariable Long orderId,
                                      @RequestBody Delivery d){
        System.out.println("===== [C-DV] PUT /api/orders/"+orderId+"/delivery =====");
        requireUser(token);
        try {
            var saved = flow.upsertDelivery(orderId, d);
            System.out.println("[C-DV] OK savedId=" + saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            System.out.println("[C-DV] EXCEPTION: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @PutMapping("/{orderId}/payment")
    public ResponseEntity<?> payment(@RequestHeader("X-SESSION-TOKEN") String token,
                                     @PathVariable Long orderId,
                                     @RequestBody Payment p){
        System.out.println("===== [C-PM] PUT /api/orders/"+orderId+"/payment =====");
        requireUser(token);
        try {
            var saved = flow.upsertPayment(orderId, p);
            System.out.println("[C-PM] OK savedId=" + saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            System.out.println("[C-PM] EXCEPTION: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @PostMapping("/{orderId}/ps")
    public ResponseEntity<?> createPs(@RequestHeader("X-SESSION-TOKEN") String token,
                                      @PathVariable Long orderId,
                                      @RequestBody PsRecord ps){
        System.out.println("===== [C-PS] POST /api/orders/"+orderId+"/ps =====");
        requireUser(token);
        try {
            var saved = flow.createPsRecord(orderId, ps);
            System.out.println("[C-PS] OK savedId=" + saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            System.out.println("[C-PS] EXCEPTION: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @PutMapping("/ps/{recordId}/resolve")
    public ResponseEntity<?> resolvePs(@RequestHeader("X-SESSION-TOKEN") String token,
                                       @PathVariable Long recordId){
        System.out.println("===== [C-PS-RES] PUT /api/orders/ps/"+recordId+"/resolve =====");
        requireUser(token);
        try {
            var saved = flow.resolvePs(recordId);
            System.out.println("[C-PS-RES] OK recordId=" + saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            System.out.println("[C-PS-RES] EXCEPTION: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@RequestHeader("X-SESSION-TOKEN") String token,
                                         @PathVariable Long orderId){
        System.out.println("===== [C-DEL] DELETE /api/orders/"+orderId+" =====");
        requireUser(token);
        try {
            flow.deleteOrderCascade(orderId);
            System.out.println("[C-DEL] OK deleted orderId=" + orderId);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception ex) {
            System.out.println("[C-DEL] EXCEPTION: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }
}