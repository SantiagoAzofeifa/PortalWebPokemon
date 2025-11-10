package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.*;
import cr.ac.una.portalwebpokeapi.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio que orquesta el ciclo de vida de una orden.
 *
 * Funciones:
 * - Checkout desde el carrito del usuario → crea Order y OrderItems.
 * - Lectura detallada de una orden con sus submódulos (warehouse, packaging, delivery, payment).
 * - Upsert de cada submódulo de forma independiente.
 * - Eliminación en cascada de la orden y sus dependencias.
 *
 * Transaccionalidad:
 * - Métodos de mutación con @Transactional para garantizar consistencia.
 * - Lecturas marcadas como readOnly cuando aplica.
 */
@Service
public class OrderFlowService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final WarehouseRepository warehouseRepo;
    private final PackagingRepository packagingRepo;
    private final DeliveryRepository deliveryRepo;
    private final PaymentRepository paymentRepo;
    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;

    public OrderFlowService(OrderRepository orderRepo,
                            OrderItemRepository orderItemRepo,
                            WarehouseRepository warehouseRepo,
                            PackagingRepository packagingRepo,
                            DeliveryRepository deliveryRepo,
                            PaymentRepository paymentRepo,
                            CartRepository cartRepo,
                            CartItemRepository cartItemRepo) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.warehouseRepo = warehouseRepo;
        this.packagingRepo = packagingRepo;
        this.deliveryRepo = deliveryRepo;
        this.paymentRepo = paymentRepo;
        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
    }

    /**
     * Crea una orden a partir del carrito del usuario.
     * Copia todas las líneas del carrito a order_items y limpia el carrito.
     *
     * Reglas:
     * - Requiere carrito existente y con ítems.
     * - Inicializa campos base: userId, status (por defecto CREATED), createdAt.
     */
    @Transactional
    public Order checkoutFromCart(Long userId, Order base) {
        Cart cart = cartRepo.findByUserId(userId).orElse(null);
        if (cart == null) throw new IllegalArgumentException("Carrito vacío");
        List<CartItem> items = cartItemRepo.findByCartId(cart.getId());
        if (items.isEmpty()) throw new IllegalArgumentException("Carrito vacío");

        // Sanitiza entidad base para persistencia nueva
        base.setId(null);
        base.setUserId(userId);
        if (base.getStatus() == null || base.getStatus().isBlank()) base.setStatus("CREATED");
        if (base.getCreatedAt() == null) base.setCreatedAt(Instant.now());

        Order saved = orderRepo.save(base);

        // Copia cada línea del carrito a order_items congelando precio y cantidad
        for (CartItem ci : items) {
            OrderItem oi = new OrderItem();
            oi.setOrderId(saved.getId());
            oi.setProductId(ci.getProductId());
            oi.setProductCategory(ci.getProductCategory());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getUnitPrice());
            orderItemRepo.save(oi);
        }

        // Limpia el carrito del usuario
        cartItemRepo.deleteByCartId(cart.getId());
        return saved;
    }

    /**
     * Devuelve vista agregada de una orden y sus módulos vinculados.
     * Incluye: order, items, warehouse, packaging, delivery, payment.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> viewOrder(Long orderId) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        Warehouse wh = warehouseRepo.findByOrderId(orderId).orElse(null);
        Packaging pk = packagingRepo.findByOrderId(orderId).orElse(null);
        Delivery dv = deliveryRepo.findByOrderId(orderId).orElse(null);
        Payment pm = paymentRepo.findByOrderId(orderId).orElse(null);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("order", o);
        map.put("items", items);
        map.put("warehouse", wh);
        map.put("packaging", pk);
        map.put("delivery", dv);
        map.put("payment", pm);
        return map;
    }

    /**
     * Lista órdenes por usuario.
     */
    @Transactional(readOnly = true)
    public List<Order> listForUser(Long userId) {
        return orderRepo.findByUserId(userId);
    }

    /**
     * Lista órdenes para todos los usuarios.
     */
    @Transactional(readOnly = true)
    public List<Order> listForAllUsers() {
        return orderRepo.findAll();
    }

    /**
     * Crea o actualiza Warehouse asociado a la orden.
     * Si no existe, crea con orderId; si existe, actualiza campos.
     * inDate por defecto a ahora si no viene informado.
     */
    @Transactional
    public Warehouse upsertWarehouse(Long orderId, Warehouse data) {
        Warehouse w = warehouseRepo.findByOrderId(orderId).orElse(new Warehouse());
        w.setOrderId(orderId);
        w.setInDate(data.getInDate() == null ? Instant.now() : data.getInDate());
        w.setOutDate(data.getOutDate());
        w.setStockChecked(data.getStockChecked());
        w.setStockQty(data.getStockQty());
        w.setLocation(data.getLocation());
        w.setOriginCountry(data.getOriginCountry());
        w.setNotes(data.getNotes());
        return warehouseRepo.save(w);
    }

    /**
     * Crea o actualiza Packaging asociado a la orden.
     */
    @Transactional
    public Packaging upsertPackaging(Long orderId, Packaging data) {
        Packaging p = packagingRepo.findByOrderId(orderId).orElse(new Packaging());
        p.setOrderId(orderId);
        p.setSize(data.getSize());
        p.setType(data.getType());
        p.setMaterials(data.getMaterials());
        p.setFragile(data.getFragile());
        p.setNotes(data.getNotes());
        return packagingRepo.save(p);
    }

    /**
     * Crea o actualiza Delivery asociado a la orden.
     */
    @Transactional
    public Delivery upsertDelivery(Long orderId, Delivery data) {
        Delivery d = deliveryRepo.findByOrderId(orderId).orElse(new Delivery());
        d.setOrderId(orderId);
        d.setMethod(data.getMethod());
        d.setAddress(data.getAddress());
        d.setScheduledDate(data.getScheduledDate());
        d.setTrackingCode(data.getTrackingCode());
        d.setNotes(data.getNotes());
        return deliveryRepo.save(d);
    }

    /**
     * Crea o actualiza Payment asociado a la orden.
     * Calcula itemCount, grossAmount y netAmount en base a order_items.
     * paidAt por defecto a ahora si no viene informado.
     */
    @Transactional
    public Payment upsertPayment(Long orderId, Payment data) {
        Payment p = paymentRepo.findByOrderId(orderId).orElse(new Payment());
        p.setOrderId(orderId);
        p.setCurrency(data.getCurrency());

        var items = orderItemRepo.findByOrderId(orderId);
        p.setItemCount(items.size());
        double gross = items.stream().mapToDouble(i -> i.getUnitPrice() * i.getQuantity()).sum();
        p.setGrossAmount(gross);
        p.setNetAmount(gross); // sin descuentos/impuestos en este flujo
        p.setMethod(data.getMethod());
        p.setPaidAt(data.getPaidAt() == null ? Instant.now() : data.getPaidAt());
        p.setNotes(data.getNotes());
        return paymentRepo.save(p);
    }

    /**
     * Elimina una orden y todas sus entidades relacionadas.
     * Orden de borrado: items → warehouse → packaging → delivery → payment → order.
     * Idempotente a nivel de API si se invoca sobre una orden inexistente.
     */
    @Transactional
    public void deleteOrderCascade(Long orderId) {
        orderItemRepo.deleteByOrderId(orderId);
        warehouseRepo.findByOrderId(orderId).ifPresent(warehouseRepo::delete);
        packagingRepo.findByOrderId(orderId).ifPresent(packagingRepo::delete);
        deliveryRepo.findByOrderId(orderId).ifPresent(deliveryRepo::delete);
        paymentRepo.findByOrderId(orderId).ifPresent(paymentRepo::delete);
        orderRepo.deleteById(orderId);
    }
}
