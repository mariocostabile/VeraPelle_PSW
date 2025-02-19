package psw.verapelle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psw.verapelle.DTO.OrderDTO;
import psw.verapelle.entity.Customer;
import psw.verapelle.entity.Order;
import psw.verapelle.entity.Product;
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

    public Order createOrder (OrderDTO orderDTO){
        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        List<Product> products = productRepository.findAllById(orderDTO.getProductIds());
        Order order = new Order(null, customer, products, orderDTO.getTotalAmount(), LocalDateTime.now());
        return orderRepository.save(order);
    }
}
