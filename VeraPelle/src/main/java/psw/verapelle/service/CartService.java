package psw.verapelle.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psw.verapelle.entity.Cart;
import psw.verapelle.entity.Customer;
import psw.verapelle.repository.CartRepository;
import psw.verapelle.repository.CustomerRepository;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public Cart getCartByCustomer(Long customerId) {
        return cartRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Cart createCart(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Cart cart = new Cart();
        cart.setCustomer(customer);
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }
}

