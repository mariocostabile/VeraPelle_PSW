package psw.verapelle.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private ProductDTO product;
    private int quantity;
    private double subtotal;
    private String thumbnailUrl;
    private ColorDTO selectedColor;

    /** Stock residuo della variante selezionata */
    private int variantStockQuantity;
}
