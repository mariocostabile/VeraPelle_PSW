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

    /**
     * Nome del colore selezionato per la variante
     */
    private String colorName;

    /**
     * Codice esadecimale del colore selezionato
     */
    private String colorHex;

    /**
     * Stock residuo della variante selezionata al momento dell'ordine
     */
    private int variantStockQuantity;
}
