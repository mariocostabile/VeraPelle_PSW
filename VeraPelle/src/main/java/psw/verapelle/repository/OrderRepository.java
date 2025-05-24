package psw.verapelle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import psw.verapelle.entity.Orders;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {

    /**
     * Trova tutti gli ordini il cui customer.id == customerId
     */
    List<Orders> findByCustomer_Id(String customerId);

    /**
     * Trova l'ordine per id E per customer.id == customerId,
     * così l'utente non vede ordini di altri.
     */
    Optional<Orders> findByIdAndCustomer_Id(Long id, String customerId);
}
