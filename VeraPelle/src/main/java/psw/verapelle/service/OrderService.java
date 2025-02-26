package psw.verapelle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import psw.verapelle.DTO.OrderDTO;
import psw.verapelle.entity.*;
import psw.verapelle.repository.CartRepository;
import psw.verapelle.repository.CustomerRepository;
import psw.verapelle.repository.OrderRepository;
import psw.verapelle.repository.ProductRepository;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    public Order createOrder (OrderDTO orderDTO){
        // Recupero l'utente autenticato
        String email = getAuthenticatedUserEmail();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Recupero il carrello dell'utente
        Cart cart = cartRepository.findByCustomerId(customer.getId());
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty. Add items before placing an order.");
        }

        // Controllo nello stock e lo aggiorno
        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        // Creo l'ordine
        List<Product> products = cart.getCartItems().stream().map(CartItem::getProduct).toList();
        Order order = new Order(null, customer, products, orderDTO.getTotalAmount(), LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // Svuoto il carrello
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    private String getAuthenticatedUserEmail() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("email");
    }
}
