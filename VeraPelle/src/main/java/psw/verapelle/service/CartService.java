package psw.verapelle.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import psw.verapelle.entity.Cart;
import psw.verapelle.entity.Customer;
import psw.verapelle.repository.CartItemRepository;
import psw.verapelle.repository.CartRepository;
import psw.verapelle.repository.CustomerRepository;

import java.util.Optional;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public Cart getCartByCustomer() {
        String email = getAuthenticatedUserEmail();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return Optional.ofNullable(cartRepository.findByCustomerId(customer.getId()))
                .orElseGet(() -> createCart(customer));
    }

    @Transactional
    public Cart createCart(Customer customer) {
        Cart cart = new Cart();
        cart.setCustomer(customer);
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long cartId) {
        Cart cart = getCartByCustomer();
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    private String getAuthenticatedUserEmail() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("email");
    }
}

