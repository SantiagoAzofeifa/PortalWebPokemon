package cr.ac.una.portalwebpokeapi.dto;
import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor
public class ProductDTO {
    private String category;
    private String externalId;
    private String name;
    private String description;
    private String imageUrl;
    private Double price;
    private String currency;
}
