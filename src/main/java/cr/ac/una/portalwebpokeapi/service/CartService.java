package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.Cart;
import cr.ac.una.portalwebpokeapi.model.CartItem;
import cr.ac.una.portalwebpokeapi.repository.CartItemRepository;
import cr.ac.una.portalwebpokeapi.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final PokeApiService poke;

    @Transactional
    public Cart getOrCreate(Long userId) {
        return cartRepo.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart(); c.setUserId(userId); return cartRepo.save(c);
        });
    }

    @Transactional
    public void addPokemon(Long userId, String nameOrId, int qty) {
        if (qty < 1) qty = 1;
        Cart cart = getOrCreate(userId);
        // Siempre llamar en minúscula (PokeAPI es case insensitive en mayoría de endpoints pero evitamos sorpresas)
        Map<String,Object> detail = poke.getPokemon(nameOrId.toLowerCase());
        if (detail == null || detail.get("id") == null)
            throw new IllegalArgumentException("Pokémon no encontrado");

        Long pid = ((Number)detail.get("id")).longValue();
        double price = PokeCatalogService.priceFromDetail(detail);

        CartItem existing = itemRepo.findByCartId(cart.getId()).stream()
                .filter(ci -> ci.getProductId().equals(pid))
                .findFirst().orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + qty);
            itemRepo.save(existing);
        } else {
            CartItem it = new CartItem();
            it.setCartId(cart.getId());
            it.setProductId(pid);
            it.setQuantity(qty);
            it.setUnitPrice(price);
            itemRepo.save(it);
        }
    }

    @Transactional
    public void updateQty(Long userId, Long itemId, int qty) {
        if (qty < 1) qty = 1;
        Cart cart = getOrCreate(userId);
        CartItem it = itemRepo.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));
        if (!it.getCartId().equals(cart.getId())) throw new SecurityException("FORBIDDEN");
        it.setQuantity(qty);
        itemRepo.save(it);
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreate(userId);
        CartItem it = itemRepo.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));
        if (!it.getCartId().equals(cart.getId())) throw new SecurityException("FORBIDDEN");
        itemRepo.delete(it);
    }

    @Transactional
    public void clear(Long userId) {
        Cart cart = getOrCreate(userId);
        itemRepo.deleteByCartId(cart.getId());
    }

    @Transactional(readOnly = true)
    public Map<String,Object> view(Long userId) {
        Cart cart = getOrCreate(userId);
        List<CartItem> items = itemRepo.findByCartId(cart.getId());
        List<Map<String,Object>> enhanced = new ArrayList<>();
        double total = 0d;

        for (CartItem ci : items) {
            Map<String,Object> detail = poke.getPokemon(String.valueOf(ci.getProductId()));
            String name = detail == null ? ("#" + ci.getProductId()) : String.valueOf(detail.get("name"));
            String image = detail == null ? null : PokeCatalogService.spriteFromDetail(detail);
            Map<String,Object> row = new LinkedHashMap<>();
            row.put("id", ci.getId());
            row.put("productId", ci.getProductId());
            row.put("name", name);
            row.put("image", image);
            row.put("quantity", ci.getQuantity());
            row.put("unitPrice", ci.getUnitPrice());
            row.put("lineTotal", ci.getUnitPrice() * ci.getQuantity());
            enhanced.add(row);
            total += (ci.getUnitPrice() * ci.getQuantity());
        }

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("cartId", cart.getId());
        out.put("items", enhanced);
        out.put("total", total);
        return out;
    }
}