package psw.verapelle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import psw.verapelle.entity.Order;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
