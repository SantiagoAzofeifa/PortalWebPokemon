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
    public void addCatalog(Long userId, String category, String nameOrId, int qty) {
        if (qty < 1) qty = 1;
        String cat = (category==null? "POKEMON" : category.trim().toUpperCase());
        Cart cart = getOrCreate(userId);

        Long pid;
        double price;

        switch (cat) {
            case "POKEMON" -> {
                Map<String,Object> detail = poke.getPokemon(nameOrId.toLowerCase());
                if (detail == null || detail.get("id") == null) throw new IllegalArgumentException("Pokémon no encontrado");
                pid = ((Number)detail.get("id")).longValue();
                price = PokeCatalogService.priceFromPokemonDetail(detail);
            }
            case "ITEM" -> {
                Map<String,Object> detail = poke.getItem(nameOrId.toLowerCase());
                if (detail == null || detail.get("id") == null) throw new IllegalArgumentException("Item no encontrado");
                pid = ((Number)detail.get("id")).longValue();
                price = PokeCatalogService.priceFromItemDetail(detail);
            }
            case "GAME" -> {
                Map<String,Object> detail = poke.getVersion(nameOrId.toLowerCase());
                if (detail == null || detail.get("id") == null) throw new IllegalArgumentException("Juego no encontrado");
                pid = ((Number)detail.get("id")).longValue();
                price = PokeCatalogService.priceFromVersion(detail);
            }
            default -> throw new IllegalArgumentException("Categoría inválida: " + category);
        }

        // Buscar existente por (productId + productCategory)
        CartItem existing = itemRepo.findByCartId(cart.getId()).stream()
                .filter(ci -> ci.getProductId().equals(pid) && cat.equals(ci.getProductCategory()))
                .findFirst().orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + qty);
            itemRepo.save(existing);
        } else {
            CartItem it = new CartItem();
            it.setCartId(cart.getId());
            it.setProductId(pid);
            it.setProductCategory(cat);
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
            String cat = ci.getProductCategory();
            String name = "#"+ci.getProductId();
            String image = null;

            try {
                switch (cat) {
                    case "POKEMON" -> {
                        Map<String,Object> d = poke.getPokemon(String.valueOf(ci.getProductId()));
                        if (d!=null) { name = String.valueOf(d.get("name")); image = PokeCatalogService.spriteFromPokemonDetail(d); }
                    }
                    case "ITEM" -> {
                        Map<String,Object> d = poke.getItem(String.valueOf(ci.getProductId()));
                        if (d!=null) { name = String.valueOf(d.get("name")); image = PokeCatalogService.imageFromItemDetail(d); }
                    }
                    case "GAME" -> {
                        Map<String,Object> d = poke.getVersion(String.valueOf(ci.getProductId()));
                        if (d!=null) { name = String.valueOf(d.get("name")); image = null; }
                    }
                }
            } catch (Exception ignored) {}

            Map<String,Object> row = new LinkedHashMap<>();
            row.put("id", ci.getId());
            row.put("productId", ci.getProductId());
            row.put("productCategory", ci.getProductCategory());
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