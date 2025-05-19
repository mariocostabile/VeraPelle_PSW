package psw.verapelle.DTO;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    // ← sostituito categoryId con lista di ID
    private List<Long> categoryIds;

    // ← aggiunto: lista di ID dei colori associati
    private List<Long> colorIds;
}
