package psw.verapelle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import psw.verapelle.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
