package psw.verapelle.DTO;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInfoDTO {
    /**
     * Numero carta (puoi simulare il masking lato client)
     */
    private String cardNumber;

    /**
     * Mese/anno di scadenza, es. "09/27"
     */
    private String expiry;

    /**
     * Codice di sicurezza (CVV/CVC)
     */
    private String cvv;
}
