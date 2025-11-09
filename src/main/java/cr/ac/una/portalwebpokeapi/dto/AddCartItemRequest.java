package cr.ac.una.portalwebpokeapi.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class AddCartItemRequest {
    private String category;
    private String externalId;
    private String name;
    private Integer quantity;
    private Double unitPrice;
    private String imageUrl;
}

