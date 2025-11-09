package cr.ac.una.portalwebpokeapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Servicio de catálogo que normaliza datos de PokeAPI en “cards”
 * (POKEMON, ITEM, GAME) y calcula precios e imágenes.
 *
 * Responsabilidades:
 *  - Adaptar respuestas de PokeAPI a estructuras planas para UI.
 *  - Calcular precios determinísticos a partir de los detalles.
 *  - Paginar y filtrar listados por nombre.
 *  - Unificar resultados por categoría.
 */
@Service
@RequiredArgsConstructor
public class PokeCatalogService {

    private final PokeApiService poke;

    // ==================== Utilidades de precio e imagen (POKEMON) ====================

    /**
     * Precio heurístico para un Pokémon:
     * base_experience*0.6 + weight*0.1 + (id%10). Mínimo 5.00. Redondeo HALF_UP a 2 decimales.
     */
    public static double priceFromPokemonDetail(Map<String, Object> detail) {
        Number baseExp = (Number) detail.getOrDefault("base_experience", 50);
        Number weight = (Number) detail.getOrDefault("weight", 100);
        Number id = (Number) detail.getOrDefault("id", 1);
        double raw = baseExp.doubleValue() * 0.6 + weight.doubleValue() * 0.1 + (id.longValue() % 10);
        double val = Math.max(5.0, raw);
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Obtiene el sprite principal del Pokémon.
     * Busca en sprites.front_default y como fallback el SVG de dream_world.
     */
    @SuppressWarnings("unchecked")
    public static String spriteFromPokemonDetail(Map<String,Object> detail) {
        Map<String,Object> sprites = (Map<String,Object>) detail.get("sprites");
        if (sprites == null) return null;
        Object def = sprites.get("front_default");
        if (def != null) return def.toString();
        Object other = sprites.get("other");
        if (other instanceof Map<?,?> mOther) {
            Object dream = mOther.get("dream_world");
            if (dream instanceof Map<?,?> mDream) {
                Object svg = mDream.get("front_default");
                if (svg != null) return svg.toString();
            }
        }
        return null;
    }

    /**
     * Extrae los tipos elementales del Pokémon como lista de strings.
     */
    @SuppressWarnings("unchecked")
    public static List<String> typesFromPokemonDetail(Map<String,Object> detail) {
        List<Map<String,Object>> types = (List<Map<String,Object>>) detail.get("types");
        if (types == null) return List.of();
        List<String> out = new ArrayList<>();
        for (Map<String,Object> t : types) {
            Object type = t.get("type");
            if (type instanceof Map<?,?> tm) {
                Object name = ((Map<?,?>) type).get("name");
                if (name != null) out.add(name.toString());
            }
        }
        return out;
    }

    /**
     * Normaliza el detalle de Pokémon a una “card” para UI.
     * Keys: id, name, image, types, price, kind="POKEMON".
     */
    public Map<String,Object> toPokemonCard(Map<String,Object> detail) {
        Map<String,Object> card = new LinkedHashMap<>();
        card.put("id", detail.get("id"));
        card.put("name", detail.get("name"));
        card.put("image", spriteFromPokemonDetail(detail));
        card.put("types", typesFromPokemonDetail(detail));
        card.put("price", priceFromPokemonDetail(detail));
        card.put("kind", "POKEMON");
        return card;
    }

    // =============================== Utilidades (ITEM) ===============================

    /**
     * Devuelve la imagen del ítem. Usa sprites.default si existe.
     */
    @SuppressWarnings("unchecked")
    public static String imageFromItemDetail(Map<String,Object> detail) {
        Map<String,Object> sprites = (Map<String,Object>) detail.get("sprites");
        if (sprites == null) return null;
        Object def = sprites.get("default");
        return def==null? null : def.toString();
    }

    /**
     * Precio heurístico para un ítem:
     * max(1.00, cost/10 + 2). Redondeo HALF_UP a 2 decimales.
     */
    public static double priceFromItemDetail(Map<String,Object> detail) {
        Number cost = (Number) detail.getOrDefault("cost", 100);
        double val = Math.max(1.0, cost.doubleValue() / 10.0 + 2.0);
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Normaliza el detalle de ítem a “card”.
     * Keys: id, name, image, price, kind="ITEM".
     */
    public Map<String,Object> toItemCard(Map<String,Object> detail) {
        Map<String,Object> card = new LinkedHashMap<>();
        card.put("id", detail.get("id"));
        card.put("name", detail.get("name"));
        card.put("image", imageFromItemDetail(detail));
        card.put("price", priceFromItemDetail(detail));
        card.put("kind", "ITEM");
        return card;
    }

    // =============================== Utilidades (GAME) ===============================

    /**
     * Precio heurístico para una versión de juego:
     * 50.00 + id*2.00. Redondeo HALF_UP a 2 decimales.
     */
    public static double priceFromVersion(Map<String,Object> detail) {
        Number id = (Number) detail.getOrDefault("id", 1);
        double val = 50.0 + id.intValue() * 2.0;
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Normaliza el detalle de una versión de juego a “card”.
     * Keys: id, name, image=null (placeholder en front), price, kind="GAME".
     */
    public Map<String,Object> toGameCard(Map<String,Object> detail) {
        Map<String,Object> card = new LinkedHashMap<>();
        card.put("id", detail.get("id"));
        card.put("name", detail.get("name"));
        card.put("image", null); // placeholder en front si null
        card.put("price", priceFromVersion(detail));
        card.put("kind", "GAME");
        return card;
    }

    // ================================ Listados por tipo ================================

    /**
     * Lista “cards” de Pokémon usando list -> get por nombre.
     * Aplica filtro por nombre (contains) si query no es blanco.
     * type está reservado para futuros filtros por tipo elemental.
     */
    public List<Map<String,Object>> listPokemonCards(Integer limit, Integer offset, String query, String type) {
        limit = sanitizeLimit(limit); offset = sanitizeOffset(offset);
        Map<String,Object> base = poke.listPokemon(limit, offset);
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> seeds = (List<Map<String,Object>>) base.getOrDefault("results", List.of());
        if (query!=null && !query.isBlank()) {
            String q = query.toLowerCase();
            seeds = seeds.stream().filter(m -> ((String)m.get("name")).contains(q)).toList();
        }
        List<Map<String,Object>> out = new ArrayList<>();
        for (Map<String,Object> r : seeds) {
            String name = String.valueOf(r.get("name"));
            Map<String,Object> detail = poke.getPokemon(name);
            out.add(toPokemonCard(detail));
        }
        return out;
    }

    /**
     * Lista “cards” de ítems con filtro por nombre.
     */
    public List<Map<String,Object>> listItemCards(Integer limit, Integer offset, String query) {
        limit = sanitizeLimit(limit); offset = sanitizeOffset(offset);
        Map<String,Object> base = poke.listItems(limit, offset);
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> seeds = (List<Map<String,Object>>) base.getOrDefault("results", List.of());
        if (query!=null && !query.isBlank()) {
            String q = query.toLowerCase();
            seeds = seeds.stream().filter(m -> ((String)m.get("name")).contains(q)).toList();
        }
        List<Map<String,Object>> out = new ArrayList<>();
        for (Map<String,Object> r : seeds) {
            String name = String.valueOf(r.get("name"));
            Map<String,Object> detail = poke.getItem(name);
            out.add(toItemCard(detail));
        }
        return out;
    }

    /**
     * Lista “cards” de juegos (versions) con filtro por nombre.
     */
    public List<Map<String,Object>> listGameCards(Integer limit, Integer offset, String query) {
        limit = sanitizeLimit(limit); offset = sanitizeOffset(offset);
        Map<String,Object> base = poke.listVersions(limit, offset);
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> seeds = (List<Map<String,Object>>) base.getOrDefault("results", List.of());
        if (query!=null && !query.isBlank()) {
            String q = query.toLowerCase();
            seeds = seeds.stream().filter(m -> ((String)m.get("name")).contains(q)).toList();
        }
        List<Map<String,Object>> out = new ArrayList<>();
        for (Map<String,Object> r : seeds) {
            String name = String.valueOf(r.get("name"));
            Map<String,Object> detail = poke.getVersion(name);
            out.add(toGameCard(detail));
        }
        return out;
    }

    // ================================= Agregador unificado =================================

    /**
     * Unifica “cards” de POKEMON, ITEM y GAME.
     * Si category != ALL, delega al listado específico.
     * Si category = ALL, concatena, ordena por name y pagina en memoria.
     */
    public List<Map<String,Object>> listUnifiedCards(Integer limit, Integer offset, String query, String type, String category) {
        limit = sanitizeLimit(limit); offset = sanitizeOffset(offset);
        String cat = (category==null? "ALL" : category.trim().toUpperCase());

        if (!"ALL".equals(cat)) {
            return switch (cat) {
                case "POKEMON" -> listPokemonCards(limit, offset, query, type);
                case "ITEM" -> listItemCards(limit, offset, query);
                case "GAME" -> listGameCards(limit, offset, query);
                default -> List.of();
            };
        }

        // ALL: trae un bloque de cada uno y mezcla
        List<Map<String,Object>> p = listPokemonCards(limit, 0, query, type);
        List<Map<String,Object>> i = listItemCards(limit, 0, query);
        List<Map<String,Object>> g = listGameCards(limit, 0, query);

        List<Map<String,Object>> merged = new ArrayList<>();
        merged.addAll(p); merged.addAll(i); merged.addAll(g);

        // Orden estable por nombre
        merged.sort(Comparator.comparing(m -> String.valueOf(m.get("name"))));

        int from = Math.min(offset, merged.size());
        int to = Math.min(offset + limit, merged.size());
        return merged.subList(from, to);
    }

    // ================================= Sanitización de paginación =================================

    /** Limita tamaño de página a [1, 50]. Por defecto 20. */
    private int sanitizeLimit(Integer limit) {
        return (limit==null || limit < 1) ? 20 : Math.min(50, limit);
    }

    /** Offset mínimo 0. Por defecto 0. */
    private int sanitizeOffset(Integer offset) {
        return (offset==null || offset < 0) ? 0 : offset;
    }
}
