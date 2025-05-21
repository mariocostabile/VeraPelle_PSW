package psw.verapelle.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCartItemRequest {
    private Long productId;
    private Long colorId;
    private int quantity;
}
