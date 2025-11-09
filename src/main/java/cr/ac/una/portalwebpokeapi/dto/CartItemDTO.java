package cr.ac.una.portalwebpokeapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private String category;
    private String externalId;
    private String name;
    private Integer quantity;
    private Double unitPrice;
    private String currency;
    private String imageUrl;
}
