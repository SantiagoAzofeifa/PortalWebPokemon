package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.*;
import cr.ac.una.portalwebpokeapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
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

    @Transactional
    public Order checkoutFromCart(Long userId, Order base) {
        Cart cart = cartRepo.findByUserId(userId).orElse(null);
        if (cart == null) throw new IllegalArgumentException("Carrito vacío");
        List<CartItem> items = cartItemRepo.findByCartId(cart.getId());
        if (items.isEmpty()) throw new IllegalArgumentException("Carrito vacío");

        base.setId(null);
        base.setUserId(userId);
        Order saved = orderRepo.save(base);

        for (CartItem ci : items) {
            OrderItem oi = new OrderItem();
            oi.setOrderId(saved.getId());
            oi.setProductId(ci.getProductId());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getUnitPrice());
            orderItemRepo.save(oi);
        }
        cartItemRepo.deleteByCartId(cart.getId()); // limpiar carrito tras checkout
        return saved;
    }

    // (Resto de métodos de etapas ya vistos)
}