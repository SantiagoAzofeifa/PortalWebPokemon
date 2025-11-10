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

/**
 * Orquestador del flujo de pedidos.
 * Expone endpoints para checkout y actualización de submódulos
 * (warehouse, packaging, delivery, payment) de una orden.
 *
 * Base: /api/orders
 * Seguridad: requiere header X-SESSION-TOKEN válido.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderFlowController {

    private final OrderFlowService flow;   // Lógica de negocio del ciclo de vida de órdenes
    private final SessionManager sessions; // Resolución de usuario desde el token

    public OrderFlowController(OrderFlowService flow, SessionManager sessions) {
        this.flow = flow;
        this.sessions = sessions;
    }

    /**
     * Verifica sesión y devuelve userId. Lanza SecurityException("UNAUTHORIZED") si no hay sesión.
     */
    private Long requireUser(String token){
        var s = sessions.get(token);
        if (s == null) throw new SecurityException("UNAUTHORIZED");
        return Long.valueOf(s.userId);
    }

    /**
     * Crea una orden a partir del carrito del usuario y datos de entrega/contacto.
     * POST /api/orders/checkout
     *
     * Body: {@link CheckoutRequest} con datos del cliente y dirección.
     * Respuesta: 200 {"orderId": <id>} al crear correctamente.
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestHeader("X-SESSION-TOKEN") String token,
                                      @RequestBody CheckoutRequest req){
        Long userId = requireUser(token);

        // Mapea datos de checkout a entidad Order inicial
        Order order = new Order();
        order.setCustomerName(req.getCustomerName());
        order.setCustomerEmail(req.getCustomerEmail());
        order.setCustomerPhone(req.getCustomerPhone());
        order.setAddressLine1(req.getAddressLine1());
        order.setAddressLine2(req.getAddressLine2());
        order.setCountry(req.getCountry());
        order.setRegion(req.getRegion());
        order.setStatus("CREATED");

        // Delegación al servicio para tomar ítems del carrito y persistir todo
        Order saved = flow.checkoutFromCart(userId, order);
        return ResponseEntity.ok(Map.of("orderId", saved.getId()));
    }

    /**
     * Devuelve la vista detallada de una orden.
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> view(@RequestHeader("X-SESSION-TOKEN") String token,
                                  @PathVariable Long orderId){
        requireUser(token);
        return ResponseEntity.ok(flow.viewOrder(orderId));
    }



    /**
     * Lista las órdenes del usuario autenticado.
     * GET /api/orders/mine
     */
    @GetMapping("/mine")
    public ResponseEntity<?> mine(@RequestHeader("X-SESSION-TOKEN") String token){
        Long userId = requireUser(token);
        return ResponseEntity.ok(flow.listForUser(userId));
    }

    /**
     * Lista las órdenes de todos los usuarios, funcion propia de admin.
     * GET /api/orders/mine
     */
    @GetMapping("/all")
    public ResponseEntity<?> all(@RequestHeader("X-SESSION-TOKEN") String token){
        Long userId = requireUser(token);
        return ResponseEntity.ok(flow.listForAllUsers());
    }

    /**
     * Crea/actualiza datos de bodega (origen) de la orden.
     * PUT /api/orders/{orderId}/warehouse
     */
    @PutMapping("/{orderId}/warehouse")
    public ResponseEntity<?> warehouse(@RequestHeader("X-SESSION-TOKEN") String token,
                                       @PathVariable Long orderId,
                                       @RequestBody Warehouse w){
        requireUser(token);
        return ResponseEntity.ok(flow.upsertWarehouse(orderId, w));
    }

    /**
     * Crea/actualiza datos de empaque de la orden.
     * PUT /api/orders/{orderId}/packaging
     */
    @PutMapping("/{orderId}/packaging")
    public ResponseEntity<?> packaging(@RequestHeader("X-SESSION-TOKEN") String token,
                                       @PathVariable Long orderId,
                                       @RequestBody Packaging p){
        requireUser(token);
        return ResponseEntity.ok(flow.upsertPackaging(orderId, p));
    }

    /**
     * Crea/actualiza datos de entrega/envío de la orden.
     * PUT /api/orders/{orderId}/delivery
     */
    @PutMapping("/{orderId}/delivery")
    public ResponseEntity<?> delivery(@RequestHeader("X-SESSION-TOKEN") String token,
                                      @PathVariable Long orderId,
                                      @RequestBody Delivery d){
        requireUser(token);
        return ResponseEntity.ok(flow.upsertDelivery(orderId, d));
    }

    /**
     * Crea/actualiza datos de pago de la orden.
     * PUT /api/orders/{orderId}/payment
     */
    @PutMapping("/{orderId}/payment")
    public ResponseEntity<?> payment(@RequestHeader("X-SESSION-TOKEN") String token,
                                     @PathVariable Long orderId,
                                     @RequestBody Payment p){
        requireUser(token);
        return ResponseEntity.ok(flow.upsertPayment(orderId, p));
    }

    /**
     * Borra una orden y su información relacionada.
     * DELETE /api/orders/{orderId}
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@RequestHeader("X-SESSION-TOKEN") String token,
                                         @PathVariable Long orderId){
        requireUser(token);
        flow.deleteOrderCascade(orderId);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
