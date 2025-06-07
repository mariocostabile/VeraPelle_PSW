package psw.verapelle.DTO;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class VariantDTO {
    private Long colorId;
    private String colorName;
    private String hexCode;
    private Integer stockQuantity;
}
