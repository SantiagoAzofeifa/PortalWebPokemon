package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.Cart;
import cr.ac.una.portalwebpokeapi.model.CartItem;
import cr.ac.una.portalwebpokeapi.repository.CartItemRepository;
import cr.ac.una.portalwebpokeapi.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Servicio de carrito.
 *
 * Responsabilidades:
 * - Crear/obtener carrito por usuario.
 * - Agregar ítems desde fuentes externas (PokeAPI) resolviendo categoría y precio.
 * - Actualizar cantidades, eliminar ítems y limpiar carrito.
 * - Armar una vista enriquecida del carrito con nombres/imagenes.
 *
 * Concurrencia y consistencia:
 * - Se usan transacciones para operaciones de escritura y limpieza.
 * - Las lecturas “view” son readOnly y se apoyan en consultas derivadas.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final PokeApiService poke; // Adaptador a PokeAPI usado por PokeCatalogService.*

    /**
     * Obtiene el carrito del usuario o lo crea si no existe.
     * Envuelve creación en TX para evitar condiciones de carrera.
     */
    @Transactional
    public Cart getOrCreate(Long userId) {
        return cartRepo.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            return cartRepo.save(c);
        });
    }

    /**
     * Agrega un ítem al carrito según categoría y nameOrId.
     * Resuelve productId y precio consultando PokeAPI.
     * Si el ítem ya existe en el carrito (misma categoría+productId), acumula cantidad.
     *
     * @param category POKEMON | ITEM | GAME (insensible a mayúsculas)
     * @param nameOrId nombre o id según la API de origen
     * @param qty cantidad mínima 1
     */
    @Transactional
    public void addCatalog(Long userId, String category, String nameOrId, int qty) {
        if (qty < 1) qty = 1;
        String cat = (category == null ? "POKEMON" : category.trim().toUpperCase());
        Cart cart = getOrCreate(userId);

        Long pid;
        double price;

        // Resuelve id y precio según categoría
        switch (cat) {
            case "POKEMON" -> {
                Map<String,Object> detail = poke.getPokemon(nameOrId.toLowerCase());
                if (detail == null || detail.get("id") == null)
                    throw new IllegalArgumentException("Pokémon no encontrado");
                pid = ((Number) detail.get("id")).longValue();
                price = PokeCatalogService.priceFromPokemonDetail(detail);
            }
            case "ITEM" -> {
                Map<String,Object> detail = poke.getItem(nameOrId.toLowerCase());
                if (detail == null || detail.get("id") == null)
                    throw new IllegalArgumentException("Item no encontrado");
                pid = ((Number) detail.get("id")).longValue();
                price = PokeCatalogService.priceFromItemDetail(detail);
            }
            case "GAME" -> {
                Map<String,Object> detail = poke.getVersion(nameOrId.toLowerCase());
                if (detail == null || detail.get("id") == null)
                    throw new IllegalArgumentException("Juego no encontrado");
                pid = ((Number) detail.get("id")).longValue();
                price = PokeCatalogService.priceFromVersion(detail);
            }
            default -> throw new IllegalArgumentException("Categoría inválida: " + category);
        }

        // Busca línea existente por (productId + productCategory) para acumular
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
            it.setUnitPrice(price); // se congela el precio al momento de agregar
            itemRepo.save(it);
        }
    }

    /**
     * Actualiza la cantidad de un ítem del carrito. Verifica pertenencia.
     * Lanza SecurityException("FORBIDDEN") si el ítem no es del carrito del usuario.
     */
    @Transactional
    public void updateQty(Long userId, Long itemId, int qty) {
        if (qty < 1) qty = 1;
        Cart cart = getOrCreate(userId);
        CartItem it = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));
        if (!it.getCartId().equals(cart.getId()))
            throw new SecurityException("FORBIDDEN");
        it.setQuantity(qty);
        itemRepo.save(it);
    }

    /**
     * Elimina una línea del carrito. Verifica pertenencia.
     */
    @Transactional
    public void removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreate(userId);
        CartItem it = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));
        if (!it.getCartId().equals(cart.getId()))
            throw new SecurityException("FORBIDDEN");
        itemRepo.delete(it);
    }

    /**
     * Limpia todas las líneas del carrito del usuario.
     */
    @Transactional
    public void clear(Long userId) {
        Cart cart = getOrCreate(userId);
        itemRepo.deleteByCartId(cart.getId());
    }

    /**
     * Devuelve una vista enriquecida del carrito:
     * - items con name e image resueltos desde PokeAPI
     * - totales calculados
     *
     * La resolución de nombres/imagenes está en try/catch blando para no
     * romper la respuesta si la API externa falla.
     */
    @Transactional(readOnly = true)
    public Map<String,Object> view(Long userId) {
        Cart cart = getOrCreate(userId);
        List<CartItem> items = itemRepo.findByCartId(cart.getId());
        List<Map<String,Object>> enhanced = new ArrayList<>();
        double total = 0d;

        for (CartItem ci : items) {
            String cat = ci.getProductCategory();
            String name = "#" + ci.getProductId(); // fallback
            String image = null;

            try {
                switch (cat) {
                    case "POKEMON" -> {
                        Map<String,Object> d = poke.getPokemon(String.valueOf(ci.getProductId()));
                        if (d != null) {
                            name = String.valueOf(d.get("name"));
                            image = PokeCatalogService.spriteFromPokemonDetail(d);
                        }
                    }
                    case "ITEM" -> {
                        Map<String,Object> d = poke.getItem(String.valueOf(ci.getProductId()));
                        if (d != null) {
                            name = String.valueOf(d.get("name"));
                            image = PokeCatalogService.imageFromItemDetail(d);
                        }
                    }
                    case "GAME" -> {
                        Map<String,Object> d = poke.getVersion(String.valueOf(ci.getProductId()));
                        if (d != null) {
                            name = String.valueOf(d.get("name"));
                            image = null; // sin imagen en este flujo
                        }
                    }
                }
            } catch (Exception ignored) { /* evita romper la respuesta si falla la API */ }

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

            total += ci.getUnitPrice() * ci.getQuantity();
        }

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("cartId", cart.getId());
        out.put("items", enhanced);
        out.put("total", total);
        return out;
    }
}
