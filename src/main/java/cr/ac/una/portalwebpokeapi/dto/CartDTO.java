package cr.ac.una.portalwebpokeapi.dto;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class CartDTO {
    private Long id;
    private Long userId;
    private String status;
    private List<CartItemDTO> items;
}


