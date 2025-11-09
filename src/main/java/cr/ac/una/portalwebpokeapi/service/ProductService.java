package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.Category;
import cr.ac.una.portalwebpokeapi.model.Product;
import cr.ac.una.portalwebpokeapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repo;

    public Page<Product> listByCategory(Category category, Pageable pageable) {
        return repo.findByCategory(category, pageable);
    }

    public Product get(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    public Product create(Product p) {
        validate(p);
        p.setId(null);
        return repo.save(p);
    }

    public Product update(Long id, Product p) {
        Product cur = get(id);
        if (p.getName()!=null) cur.setName(p.getName());
        if (p.getCategory()!=null) cur.setCategory(p.getCategory());
        if (p.getImageUrl()!=null) cur.setImageUrl(p.getImageUrl());
        if (p.getPrice()!=null) cur.setPrice(p.getPrice());
        if (p.getCountryOfOrigin()!=null) cur.setCountryOfOrigin(p.getCountryOfOrigin());
        if (p.getAvailableCountriesCsv()!=null) cur.setAvailableCountriesCsv(p.getAvailableCountriesCsv());
        if (p.getBannedCountriesCsv()!=null) cur.setBannedCountriesCsv(p.getBannedCountriesCsv());
        if (p.getDescription()!=null) cur.setDescription(p.getDescription());
        validate(cur);
        return repo.save(cur);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("Producto no encontrado");
        repo.deleteById(id);
    }

    private void validate(Product p) {
        if (p.getCategory()==null) throw new IllegalArgumentException("Categoría requerida");
        if (p.getName()==null || p.getName().isBlank()) throw new IllegalArgumentException("Nombre requerido");
        if (p.getPrice()==null || p.getPrice()<0) throw new IllegalArgumentException("Precio inválido");
    }
}