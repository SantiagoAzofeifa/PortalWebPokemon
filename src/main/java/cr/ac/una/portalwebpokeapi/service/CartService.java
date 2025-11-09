package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.Cart;
import cr.ac.una.portalwebpokeapi.model.CartItem;
import cr.ac.una.portalwebpokeapi.model.Product;
import cr.ac.una.portalwebpokeapi.repository.CartItemRepository;
import cr.ac.una.portalwebpokeapi.repository.CartRepository;
import cr.ac.una.portalwebpokeapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final ProductRepository productRepo;

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepo.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            return cartRepo.save(c);
        });
    }

    @Transactional(readOnly = true)
    public Map<String,Object> getCartResponse(Long userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = itemRepo.findByCartId(cart.getId());
        double total = items.stream().mapToDouble(i -> i.getUnitPrice() * i.getQuantity()).sum();
        Map<String,Object> out = new HashMap<>();
        out.put("cartId", cart.getId());
        out.put("items", items);
        out.put("total", total);
        return out;
    }

    @Transactional
    public void addItem(Long userId, Long productId, int qty) {
        if (qty < 1) qty = 1;
        Cart cart = getOrCreateCart(userId);
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        CartItem existing = itemRepo.findByCartId(cart.getId()).stream()
                .filter(ci -> ci.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + qty);
            itemRepo.save(existing);
        } else {
            CartItem it = new CartItem();
            it.setCartId(cart.getId());
            it.setProductId(productId);
            it.setQuantity(qty);
            it.setUnitPrice(p.getPrice());
            itemRepo.save(it);
        }
    }

    @Transactional
    public void updateItemQuantity(Long userId, Long itemId, int qty) {
        if (qty < 1) qty = 1;
        Cart cart = getOrCreateCart(userId);
        CartItem it = itemRepo.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));
        if (!it.getCartId().equals(cart.getId())) throw new SecurityException("FORBIDDEN");
        it.setQuantity(qty);
        itemRepo.save(it);
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem it = itemRepo.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));
        if (!it.getCartId().equals(cart.getId())) throw new SecurityException("FORBIDDEN");
        itemRepo.delete(it);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        itemRepo.deleteByCartId(cart.getId());
    }
}