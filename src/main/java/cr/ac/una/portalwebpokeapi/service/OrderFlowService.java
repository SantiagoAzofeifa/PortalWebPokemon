package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.*;
import cr.ac.una.portalwebpokeapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service @RequiredArgsConstructor
public class OrderFlowService {
    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final WarehouseRepository warehouseRepo;
    private final PackagingRepository packagingRepo;
    private final DeliveryRepository deliveryRepo;
    private final PaymentRepository paymentRepo;
    private final PsRecordRepository psRepo;
    private final ProductRepository productRepo;

    @Transactional
    public Order createOrder(Long userId, Order base, List<Long> productIds) {
        base.setId(null);
        base.setUserId(userId);
        Order saved = orderRepo.save(base);
        for (Long pid: productIds) {
            Product p = productRepo.findById(pid).orElseThrow(()->new IllegalArgumentException("Producto "+pid+" no encontrado"));
            OrderItem oi = new OrderItem();
            oi.setOrderId(saved.getId());
            oi.setProductId(pid);
            oi.setQuantity(1);
            oi.setUnitPrice(p.getPrice());
            itemRepo.save(oi);
        }
        return saved;
    }

    @Transactional(readOnly=true)
    public Map<String,Object> viewOrder(Long orderId) {
        Order o = orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        List<OrderItem> items = itemRepo.findByOrderId(orderId);
        var wh = warehouseRepo.findByOrderId(orderId).orElse(null);
        var pk = packagingRepo.findByOrderId(orderId).orElse(null);
        var dv = deliveryRepo.findByOrderId(orderId).orElse(null);
        var pm = paymentRepo.findByOrderId(orderId).orElse(null);
        var ps = psRepo.findByOrderId(orderId);
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("order", o);
        map.put("items", items);
        map.put("warehouse", wh);
        map.put("packaging", pk);
        map.put("delivery", dv);
        map.put("payment", pm);
        map.put("psRecords", ps);
        return map;
    }

    @Transactional
    public Warehouse upsertWarehouse(Long orderId, Warehouse data) {
        Warehouse w = warehouseRepo.findByOrderId(orderId).orElse(new Warehouse());
        w.setOrderId(orderId);
        w.setInDate(data.getInDate()==null? Instant.now(): data.getInDate());
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
        var items = itemRepo.findByOrderId(orderId);
        p.setItemCount(items.size());
        double gross = items.stream().mapToDouble(i->i.getUnitPrice()*i.getQuantity()).sum();
        p.setGrossAmount(gross);
        p.setNetAmount(gross); // aplicar descuentos si existieran
        p.setMethod(data.getMethod());
        p.setPaidAt(data.getPaidAt()==null? Instant.now(): data.getPaidAt());
        p.setNotes(data.getNotes());
        return paymentRepo.save(p);
    }

    @Transactional
    public PsRecord createPsRecord(Long orderId, PsRecord ps) {
        ps.setId(null);
        ps.setOrderId(orderId);
        ps.setCreatedAt(Instant.now());
        return psRepo.save(ps);
    }

    @Transactional
    public PsRecord resolvePs(Long recordId) {
        PsRecord r = psRepo.findById(recordId).orElseThrow(() -> new IllegalArgumentException("PS record no encontrado"));
        r.setResolved(true);
        return psRepo.save(r);
    }

    @Transactional
    public void deleteOrderCascade(Long orderId) {
        itemRepo.deleteByOrderId(orderId);
        warehouseRepo.findByOrderId(orderId).ifPresent(w->warehouseRepo.delete(w));
        packagingRepo.findByOrderId(orderId).ifPresent(p->packagingRepo.delete(p));
        deliveryRepo.findByOrderId(orderId).ifPresent(d->deliveryRepo.delete(d));
        paymentRepo.findByOrderId(orderId).ifPresent(pm->paymentRepo.delete(pm));
        psRepo.findByOrderId(orderId).forEach(ps->psRepo.delete(ps));
        orderRepo.deleteById(orderId);
    }
}