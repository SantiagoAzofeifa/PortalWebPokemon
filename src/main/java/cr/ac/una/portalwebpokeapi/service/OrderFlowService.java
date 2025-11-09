package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.Cart;
import cr.ac.una.portalwebpokeapi.model.CartItem;
import cr.ac.una.portalwebpokeapi.model.Delivery;
import cr.ac.una.portalwebpokeapi.model.Order;
import cr.ac.una.portalwebpokeapi.model.OrderItem;
import cr.ac.una.portalwebpokeapi.model.Packaging;
import cr.ac.una.portalwebpokeapi.model.Payment;
import cr.ac.una.portalwebpokeapi.model.PsRecord;
import cr.ac.una.portalwebpokeapi.model.Warehouse;
import cr.ac.una.portalwebpokeapi.repository.CartItemRepository;
import cr.ac.una.portalwebpokeapi.repository.CartRepository;
import cr.ac.una.portalwebpokeapi.repository.DeliveryRepository;
import cr.ac.una.portalwebpokeapi.repository.OrderItemRepository;
import cr.ac.una.portalwebpokeapi.repository.OrderRepository;
import cr.ac.una.portalwebpokeapi.repository.PackagingRepository;
import cr.ac.una.portalwebpokeapi.repository.PaymentRepository;
import cr.ac.una.portalwebpokeapi.repository.PsRecordRepository;
import cr.ac.una.portalwebpokeapi.repository.WarehouseRepository;
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
    private final PsRecordRepository psRepo;
    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;

    public OrderFlowService(OrderRepository orderRepo,
                            OrderItemRepository orderItemRepo,
                            WarehouseRepository warehouseRepo,
                            PackagingRepository packagingRepo,
                            DeliveryRepository deliveryRepo,
                            PaymentRepository paymentRepo,
                            PsRecordRepository psRepo,
                            CartRepository cartRepo,
                            CartItemRepository cartItemRepo) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.warehouseRepo = warehouseRepo;
        this.packagingRepo = packagingRepo;
        this.deliveryRepo = deliveryRepo;
        this.paymentRepo = paymentRepo;
        this.psRepo = psRepo;
        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
    }

    @Transactional
    public Order checkoutFromCart(Long userId, Order base) {
        System.out.println("----- [S-CHK] checkoutFromCart userId=" + userId + " -----");
        Cart cart = cartRepo.findByUserId(userId).orElse(null);
        System.out.println("[S-CHK] cart=" + (cart==null?"<null>":cart.getId()));
        if (cart == null) throw new IllegalArgumentException("Carrito vacío");
        List<CartItem> items = cartItemRepo.findByCartId(cart.getId());
        System.out.println("[S-CHK] items size=" + (items==null?0:items.size()));
        if (items.isEmpty()) throw new IllegalArgumentException("Carrito vacío");
        for (CartItem ci : items) {
            System.out.println("   [S-CHK] itemId=" + ci.getId() + " productId=" + ci.getProductId()
                    + " qty=" + ci.getQuantity() + " unitPrice=" + ci.getUnitPrice());
        }

        // Completar defaults
        if (base.getStatus() == null || base.getStatus().isBlank()) base.setStatus("CREATED");
        if (base.getCreatedAt() == null) base.setCreatedAt(Instant.now());
        base.setId(null);
        base.setUserId(userId);

        System.out.println("[S-CHK] Persistiendo orden...");
        Order saved = orderRepo.save(base);
        System.out.println("[S-CHK] Order saved id=" + saved.getId());

        for (CartItem ci : items) {
            OrderItem oi = new OrderItem();
            oi.setOrderId(saved.getId());
            oi.setProductId(ci.getProductId());
            oi.setProductCategory(ci.getProductCategory()); // NUEVO
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getUnitPrice());
            System.out.println("[S-CHK] Guardando order_item -> productId=" + oi.getProductId() + " qty=" + oi.getQuantity());
            orderItemRepo.save(oi);
        }

        System.out.println("[S-CHK] Limpiando carrito cartId=" + cart.getId());
        cartItemRepo.deleteByCartId(cart.getId());
        System.out.println("[S-CHK] Checkout OK orderId=" + saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> viewOrder(Long orderId) {
        System.out.println("----- [S-VIEW] viewOrder orderId=" + orderId + " -----");
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        var items = orderItemRepo.findByOrderId(orderId);
        var wh = warehouseRepo.findByOrderId(orderId).orElse(null);
        var pk = packagingRepo.findByOrderId(orderId).orElse(null);
        var dv = deliveryRepo.findByOrderId(orderId).orElse(null);
        var pm = paymentRepo.findByOrderId(orderId).orElse(null);
        var ps = psRepo.findByOrderId(orderId);
        System.out.println("[S-VIEW] items=" + items.size() + " wh=" + (wh!=null) + " pk=" + (pk!=null) + " dv=" + (dv!=null) + " pm=" + (pm!=null) + " ps=" + ps.size());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("order", o);
        map.put("items", items);
        map.put("warehouse", wh);
        map.put("packaging", pk);
        map.put("delivery", dv);
        map.put("payment", pm);
        map.put("psRecords", ps);
        return map;
    }

    @Transactional(readOnly = true)
    public List<Order> listForUser(Long userId) {
        System.out.println("----- [S-MINE] listForUser userId=" + userId + " -----");
        var list = orderRepo.findByUserId(userId);
        System.out.println("[S-MINE] total=" + list.size());
        return list;
    }

    @Transactional
    public Warehouse upsertWarehouse(Long orderId, Warehouse data) {
        System.out.println("----- [S-WH] upsertWarehouse orderId=" + orderId + " -----");
        Warehouse w = warehouseRepo.findByOrderId(orderId).orElse(new Warehouse());
        w.setOrderId(orderId);
        w.setInDate(data.getInDate() == null ? Instant.now() : data.getInDate());
        w.setOutDate(data.getOutDate());
        w.setStockChecked(data.getStockChecked());
        w.setStockQty(data.getStockQty());
        w.setLocation(data.getLocation());
        w.setOriginCountry(data.getOriginCountry());
        w.setNotes(data.getNotes());
        var saved = warehouseRepo.save(w);
        System.out.println("[S-WH] savedId=" + saved.getId());
        return saved;
    }

    @Transactional
    public Packaging upsertPackaging(Long orderId, Packaging data) {
        System.out.println("----- [S-PK] upsertPackaging orderId=" + orderId + " -----");
        Packaging p = packagingRepo.findByOrderId(orderId).orElse(new Packaging());
        p.setOrderId(orderId);
        p.setSize(data.getSize());
        p.setType(data.getType());
        p.setMaterials(data.getMaterials());
        p.setFragile(data.getFragile());
        p.setNotes(data.getNotes());
        var saved = packagingRepo.save(p);
        System.out.println("[S-PK] savedId=" + saved.getId());
        return saved;
    }

    @Transactional
    public Delivery upsertDelivery(Long orderId, Delivery data) {
        System.out.println("----- [S-DV] upsertDelivery orderId=" + orderId + " -----");
        Delivery d = deliveryRepo.findByOrderId(orderId).orElse(new Delivery());
        d.setOrderId(orderId);
        d.setMethod(data.getMethod());
        d.setAddress(data.getAddress());
        d.setScheduledDate(data.getScheduledDate());
        d.setTrackingCode(data.getTrackingCode());
        d.setNotes(data.getNotes());
        var saved = deliveryRepo.save(d);
        System.out.println("[S-DV] savedId=" + saved.getId());
        return saved;
    }

    @Transactional
    public Payment upsertPayment(Long orderId, Payment data) {
        System.out.println("----- [S-PM] upsertPayment orderId=" + orderId + " -----");
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
        var saved = paymentRepo.save(p);
        System.out.println("[S-PM] savedId=" + saved.getId() + " gross=" + p.getGrossAmount());
        return saved;
    }

    @Transactional
    public PsRecord createPsRecord(Long orderId, PsRecord ps) {
        System.out.println("----- [S-PS] createPsRecord orderId=" + orderId + " -----");
        ps.setId(null);
        ps.setOrderId(orderId);
        ps.setCreatedAt(Instant.now());
        var saved = psRepo.save(ps);
        System.out.println("[S-PS] savedId=" + saved.getId());
        return saved;
    }

    @Transactional
    public PsRecord resolvePs(Long recordId) {
        System.out.println("----- [S-PS-RES] resolvePs recordId=" + recordId + " -----");
        PsRecord r = psRepo.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("PS record no encontrado"));
        r.setResolved(true);
        var saved = psRepo.save(r);
        System.out.println("[S-PS-RES] savedId=" + saved.getId());
        return saved;
    }

    @Transactional
    public void deleteOrderCascade(Long orderId) {
        System.out.println("----- [S-DEL] deleteOrderCascade orderId=" + orderId + " -----");
        orderItemRepo.deleteByOrderId(orderId);
        warehouseRepo.findByOrderId(orderId).ifPresent(w -> {
            System.out.println("[S-DEL] delete warehouse id=" + w.getId());
            warehouseRepo.delete(w);
        });
        packagingRepo.findByOrderId(orderId).ifPresent(p -> {
            System.out.println("[S-DEL] delete packaging id=" + p.getId());
            packagingRepo.delete(p);
        });
        deliveryRepo.findByOrderId(orderId).ifPresent(d -> {
            System.out.println("[S-DEL] delete delivery id=" + d.getId());
            deliveryRepo.delete(d);
        });
        paymentRepo.findByOrderId(orderId).ifPresent(pm -> {
            System.out.println("[S-DEL] delete payment id=" + pm.getId());
            paymentRepo.delete(pm);
        });
        psRepo.findByOrderId(orderId).forEach(ps -> {
            System.out.println("[S-DEL] delete ps id=" + ps.getId());
            psRepo.delete(ps);
        });
        orderRepo.deleteById(orderId);
        System.out.println("[S-DEL] OK");
    }
}