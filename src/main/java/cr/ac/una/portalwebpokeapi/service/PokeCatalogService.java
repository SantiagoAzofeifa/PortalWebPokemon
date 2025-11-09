package cr.ac.una.portalwebpokeapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PokeCatalogService {

    private final PokeApiService poke;

    public static double priceFromDetail(Map<String, Object> detail) {
        Number baseExp = (Number) detail.getOrDefault("base_experience", 50);
        Number weight = (Number) detail.getOrDefault("weight", 100);
        Number id = (Number) detail.getOrDefault("id", 1);
        double raw = baseExp.doubleValue() * 0.6 + weight.doubleValue() * 0.1 + (id.longValue() % 10);
        double val = Math.max(5.0, raw);
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @SuppressWarnings("unchecked")
    public static String spriteFromDetail(Map<String,Object> detail) {
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

    @SuppressWarnings("unchecked")
    public static List<String> typesFromDetail(Map<String,Object> detail) {
        List<Map<String,Object>> types = (List<Map<String,Object>>) detail.get("types");
        if (types == null) return List.of();
        List<String> out = new ArrayList<>();
        for (Map<String,Object> t : types) {
            Object type = t.get("type");
            if (type instanceof Map<?,?> tm) {
                Object name = tm.get("name");
                if (name != null) out.add(name.toString());
            }
        }
        return out;
    }

    public Map<String,Object> toCard(Map<String,Object> detail) {
        Map<String,Object> card = new LinkedHashMap<>();
        card.put("id", detail.get("id"));
        card.put("name", detail.get("name"));
        card.put("image", spriteFromDetail(detail));
        card.put("types", typesFromDetail(detail));
        card.put("price", priceFromDetail(detail));
        return card;
    }

    public List<Map<String,Object>> listCards(Integer limit, Integer offset, String query, String type) {
        limit = (limit == null || limit < 1) ? 20 : Math.min(50, limit);
        offset = (offset == null || offset < 0) ? 0 : offset;

        List<Map<String,Object>> seeds;

        if (type != null && !type.isBlank()) {
            List<Map<String,Object>> allByType = poke.listByType(type.toLowerCase());
            seeds = allByType;
            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase();
                seeds = seeds.stream().filter(m -> ((String)m.get("name")).contains(q)).toList();
            }
            int to = Math.min(seeds.size(), offset + limit);
            if (offset >= seeds.size()) return List.of();
            seeds = seeds.subList(offset, to);
        } else {
            Map<String,Object> base = poke.listPokemon(limit, offset);
            seeds = (List<Map<String,Object>>) base.getOrDefault("results", List.of());
            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase();
                seeds = seeds.stream().filter(m -> ((String)m.get("name")).contains(q)).toList();
            }
        }

        List<Map<String,Object>> out = new ArrayList<>();
        for (Map<String,Object> r : seeds) {
            String name = String.valueOf(r.get("name"));
            Map<String,Object> detail = poke.getPokemon(name);
            out.add(toCard(detail));
        }
        return out;
    }
}