package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.dto.AddCartItemRequest;
import cr.ac.una.portalwebpokeapi.dto.CartDTO;
import cr.ac.una.portalwebpokeapi.dto.CartItemDTO;
import cr.ac.una.portalwebpokeapi.model.Cart;
import cr.ac.una.portalwebpokeapi.model.CartItem;
import cr.ac.una.portalwebpokeapi.repository.CartItemRepository;
import cr.ac.una.portalwebpokeapi.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;

    public Cart getOpenCartForUser(Long userId) {
        return cartRepo.findByUserIdAndStatus(userId, "OPEN").orElseGet(() -> {
            Cart c = Cart.builder().userId(userId).status("OPEN").build();
            return cartRepo.save(c);
        });
    }

    @Transactional
    public Cart addItem(Long userId, AddCartItemRequest req) {
        Cart cart = getOpenCartForUser(userId);
        // buscar si existe item idéntico (category + externalId)
        var existing = cart.getItems().stream()
                .filter(i -> req.getCategory().equals(i.getCategory()) &&
                        req.getExternalId().equals(i.getExternalId()))
                .findFirst();
        if (existing.isPresent()) {
            CartItem it = existing.get();
            it.setQuantity(it.getQuantity() + (req.getQuantity() == null ? 1 : req.getQuantity()));
            itemRepo.save(it);
        } else {
            CartItem it = CartItem.builder()
                    .cart(cart)
                    .category(req.getCategory())
                    .externalId(req.getExternalId())
                    .name(req.getName())
                    .quantity(req.getQuantity() == null ? 1 : req.getQuantity())
                    .unitPrice(req.getUnitPrice())
                    .currency("USD")
                    .imageUrl(req.getImageUrl())
                    .build();
            cart.getItems().add(it);
            // save cascade will persist item
        }
        cart.setUpdatedAt(java.time.Instant.now());
        return cartRepo.save(cart);
    }

    @Transactional
    public Cart updateItemQuantity(Long userId, Long itemId, Integer newQty) {
        Cart cart = getOpenCartForUser(userId);
        CartItem it = itemRepo.findById(itemId).orElseThrow();
        if (!it.getCart().getId().equals(cart.getId())) throw new RuntimeException("item no pertenece");
        if (newQty <= 0) {
            it.getCart().getItems().remove(it);
            itemRepo.delete(it);
        } else {
            it.setQuantity(newQty);
            itemRepo.save(it);
        }
        cart.setUpdatedAt(java.time.Instant.now());
        return cartRepo.save(cart);
    }

    @Transactional
    public Cart removeItem(Long userId, Long itemId) {
        Cart cart = getOpenCartForUser(userId);
        CartItem it = itemRepo.findById(itemId).orElseThrow();
        if (!it.getCart().getId().equals(cart.getId())) throw new RuntimeException("item no pertenece");
        cart.getItems().remove(it);
        itemRepo.delete(it);
        cart.setUpdatedAt(java.time.Instant.now());
        return cartRepo.save(cart);
    }

    public CartDTO toDto(Cart cart) {
        var items = cart.getItems().stream().map(i ->
                new CartItemDTO(i.getId(), i.getCategory(), i.getExternalId(), i.getName(),
                        i.getQuantity(), i.getUnitPrice(), i.getCurrency(), i.getImageUrl())
        ).collect(Collectors.toList());
        return new CartDTO(cart.getId(), cart.getUserId(), cart.getStatus(), items);
    }
}
