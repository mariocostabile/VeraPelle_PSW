package psw.verapelle.DTO;

import psw.verapelle.entity.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Long id;
    private LocalDateTime date;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;

    /**
     * Lista dettagli item
     */
    private List<OrderItemDetailDTO> items;

    /**
     * Stato simulato del pagamento ("APPROVED" o "DECLINED")
     */
    private String paymentStatus;
}
