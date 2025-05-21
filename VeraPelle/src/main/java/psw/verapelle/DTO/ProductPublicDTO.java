// src/main/java/psw/verapelle/DTO/ProductPublicDTO.java
package psw.verapelle.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductPublicDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private List<String> categoryNames;
    private String description;
    private List<String> imageUrls;
    private List<ColorDTO> colors;
    private Integer stockQuantity;
}
