package psw.verapelle.DTO;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDetailDTO {

    /**
     * Identificativo del prodotto ordinato
     */
    private Long productId;

    /**
     * Nome del prodotto (opzionale, comodo per il front-end)
     */
    private String productName;

    /**
     * Quantità ordinata
     */
    private int quantity;

    /**
     * Prezzo unitario al momento dell'ordine
     */
    private BigDecimal unitPrice;
}
