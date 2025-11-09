package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.config.PokeApiClient;
import cr.ac.una.portalwebpokeapi.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final PokeApiClient client;

    public List<ProductDTO> list(String category, int page, int size) {
        switch (category.toUpperCase()) {
            case "POKEMON": return listPokemons(page, size);
            case "SPECIES": return listSpecies(page, size);
            case "ITEMS": return listItems(page, size);
            case "GENERATIONS": return listGenerations();
            default: return List.of();
        }
    }

    public List<ProductDTO> listPokemons(int page, int size) {
        int offset = page * size;
        Map r = client.listPokemon(size, offset);
        var results = (List<Map<String, Object>>) r.get("results");
        if (results == null) return List.of();
        return results.stream().map(m -> {
            String name = (String) m.get("name");
            Map p = client.getPokemon(name);
            Integer baseExp = p.get("base_experience") == null ? 50 : ((Number)p.get("base_experience")).intValue();
            Map sprites = (Map)p.get("sprites");
            String img = sprites == null ? null : (String)sprites.get("front_default");
            double price = Math.max(9.99, baseExp * 0.25);
            return new ProductDTO("POKEMON", name, capitalize(name), "Base EXP: "+baseExp, img, price, "USD");
        }).collect(Collectors.toList());
    }

    public List<ProductDTO> listSpecies(int page, int size) {
        // reuse pokemon listing for names then fetch species
        return listPokemons(page, size).stream().map(p -> {
            Map sp = client.getSpecies(p.getExternalId());
            boolean legendary = sp != null && Boolean.TRUE.equals(sp.get("is_legendary"));
            List<Map<String,Object>> flavors = (List<Map<String, Object>>) (sp == null ? null : sp.get("flavor_text_entries"));
            String desc = p.getDescription();
            if (flavors != null) for (var f: flavors) {
                Map lang = (Map) f.get("language");
                if (lang != null && "en".equals(lang.get("name"))) {
                    desc = ((String)f.get("flavor_text")).replace('\n',' ').replace('\f',' ');
                    break;
                }
            }
            double price = legendary ? 49.99 : 19.99;
            return new ProductDTO("SPECIES", p.getExternalId(), p.getName(), desc, p.getImageUrl(), price, "USD");
        }).collect(Collectors.toList());
    }

    public List<ProductDTO> listItems(int page, int size) {
        int offset = page * size;
        Map r = client.listItems(size, offset);
        var results = (List<Map<String, Object>>) r.get("results");
        if (results == null) return List.of();
        return results.stream().map(m -> {
            String name = (String) m.get("name");
            String img = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/" + name + ".png";
            return new ProductDTO("ITEMS", name, capitalize(name), "Item oficial", img, 12.99, "USD");
        }).collect(Collectors.toList());
    }

    public List<ProductDTO> listGenerations() {
        Map r = client.listGenerations();
        var results = (List<Map<String, Object>>) r.get("results");
        if (results == null) return List.of();
        return results.stream().map(m -> {
            String name = (String) m.get("name");
            return new ProductDTO("GENERATIONS", name, capitalize(name), "Box set", null, 39.99, "USD");
        }).collect(Collectors.toList());
    }

    private static String capitalize(String s) {
        if (s==null || s.isEmpty()) return s;
        s = s.replace('-', ' ');
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}
