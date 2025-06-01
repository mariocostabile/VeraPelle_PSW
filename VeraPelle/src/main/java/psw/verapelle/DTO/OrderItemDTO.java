package psw.verapelle.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private Long productId;

    /** Variante selezionata: colore */
    @NotNull(message = "colorId è obbligatorio")
    private Long colorId;

    private int quantity;
}
