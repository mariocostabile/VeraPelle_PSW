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
    /** @deprecated gestito tramite varianti colore */
    @Deprecated
    private int stockQuantity;
    private List<Long> categoryIds;

    /** Liste delle varianti colore con stock dedicato */
    private List<VariantDTO> variants;
}
