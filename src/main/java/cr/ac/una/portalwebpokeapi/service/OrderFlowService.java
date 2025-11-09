package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.*;
import cr.ac.una.portalwebpokeapi.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Transactional
    public Order checkoutFromCart(Long userId, Order base) {
        Cart cart = cartRepo.findByUserId(userId).orElse(null);
        if (cart == null) throw new IllegalArgumentException("Carrito vacío");
        List<CartItem> items = cartItemRepo.findByCartId(cart.getId());
        if (items.isEmpty()) throw new IllegalArgumentException("Carrito vacío");

        base.setId(null);
        base.setUserId(userId);
        if (base.getStatus() == null || base.getStatus().isBlank()) base.setStatus("CREATED");
        if (base.getCreatedAt() == null) base.setCreatedAt(Instant.now());

        Order saved = orderRepo.save(base);

        for (CartItem ci : items) {
            OrderItem oi = new OrderItem();
            oi.setOrderId(saved.getId());
            oi.setProductId(ci.getProductId());
            oi.setProductCategory(ci.getProductCategory()); // mantiene categoría
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getUnitPrice());
            orderItemRepo.save(oi);
        }
        cartItemRepo.deleteByCartId(cart.getId());
        return saved;
    }

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

    @Transactional(readOnly = true)
    public List<Order> listForUser(Long userId) {
        return orderRepo.findByUserId(userId);
    }

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

    @Transactional
    public Payment upsertPayment(Long orderId, Payment data) {
        Payment p = paymentRepo.findByOrderId(orderId).orElse(new Payment());
        p.setOrderId(orderId);
        p.setCurrency(data.getCurrency());
        var items = orderItemRepo.findByOrderId(orderId);
        p.setItemCount(items.size());
        double gross = items.stream().mapToDouble(i -> i.getUnitPrice() * i.getQuantity()).sum();
        p.setGrossAmount(gross);
        p.setNetAmount(gross);
        p.setMethod(data.getMethod());
        p.setPaidAt(data.getPaidAt() == null ? Instant.now() : data.getPaidAt());
        p.setNotes(data.getNotes());
        return paymentRepo.save(p);
    }

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