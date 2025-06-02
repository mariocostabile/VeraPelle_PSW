package psw.verapelle.DTO;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    private String shippingAddress;
    private Integer cartVersion;
    // Se in futuro aggiungi dati di pagamento, inserisci qui un PaymentInfoDTO
    private PaymentInfoDTO paymentInfo;
}
