package cr.ac.una.portalwebpokeapi.repository;



import cr.ac.una.portalwebpokeapi.model.Category;
import cr.ac.una.portalwebpokeapi.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(Category category, Pageable pageable);
}