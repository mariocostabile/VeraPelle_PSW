package psw.verapelle.service;

import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import psw.verapelle.DTO.*;
import psw.verapelle.entity.*;
import psw.verapelle.repository.*;
import psw.verapelle.entity.ProductVariant;
import psw.verapelle.repository.ProductVariantRepository;


import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired private OrderRepository orderRepo;
    @Autowired private CustomerRepository customerRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private CartRepository cartRepo;



    public OrderDTO createOrder(CreateOrderRequest req, String customerId) {
        // 1) Recupera il carrello del customer da DB (ottimistic lock tramite @Version su Cart)
        Cart cart = cartRepo.findByCustomerId(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Carrello non trovato per customer: " + customerId));

        // 2) Controlla la versione: se mismatch, restituisci 409 Conflict
        if (!Objects.equals(cart.getVersion(), req.getCartVersion())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Carrello modificato da un’altra sessione. Ricarica e riprova."
            );
        }

        // 3) Recupera il Customer per associare l'ordine
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Customer non trovato: " + customerId));

        // 4) Crea e salva subito l'ordine con status CREATED
        Orders order = Orders.builder()
                .customer(customer)
                .shippingAddress(req.getShippingAddress())
                .status(OrderStatus.CREATED)
                .build();
        order = orderRepo.save(order);

        // 5) Simula il pagamento
        PaymentResult payment = processPaymentStub(order.getId(), req.getPaymentInfo());

        if (payment.approved) {
            // 6a) Se APPROVED: usiamo cart.getCartItems() per creare gli OrderItem e decurtare stock
            BigDecimal total = BigDecimal.ZERO;
            for (CartItem ci : cart.getCartItems()) {
                Long productId = ci.getProduct().getId();
                Long colorId   = ci.getSelectedColor().getId();
                int qty        = ci.getQuantity();

                // Leggi la variante colore per questo prodotto
                ProductVariant variant = variantRepository
                        .findByProductIdAndColorId(productId, colorId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Variant non trovata per product " + productId + " color " + colorId));

                // Verifica disponibilità
                if (variant.getStockQuantity() < qty) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Stock insufficiente per variante: product " + productId +
                                    " color " + colorId + ", rimangono solo " + variant.getStockQuantity());
                }

                // Decurta lo stock della variante e salva
                variant.setStockQuantity(variant.getStockQuantity() - qty);
                variantRepository.save(variant);

                // Riallinea lo stock complessivo in Product
                Product product = variant.getProduct();
                int totalStock = product.getVariants().stream()
                        .map(ProductVariant::getStockQuantity)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .sum();
                product.setStockQuantity(totalStock);
                productRepo.save(product);

                // Crea l’OrderItem con variante
                BigDecimal unitPrice = product.getPrice();
                OrderItem item = OrderItem.builder()
                        .product(product)
                        .variant(variant)
                        .quantity(qty)
                        .unitPrice(unitPrice)
                        .build();
                order.addItem(item);

                total = total.add(unitPrice.multiply(BigDecimal.valueOf(qty)));
            }

            order.setTotalAmount(total);
            order.setStatus(OrderStatus.PAID);

            // 7) Se pagamento OK, svuota il carrello
            cart.getCartItems().clear();
            cartRepo.save(cart);

        } else {
            // 6b) Se DECLINED: mantieni stock e marca l'ordine come DECLINED
            order.setStatus(OrderStatus.DECLINED);
        }

        // 8) Salva l'ordine finale (con items e stato aggiornato)
        order = orderRepo.save(order);

        // 9) Mappa e restituisci DTO
        return toDto(order, payment.message);
    }




    /**
     * Restituisce tutti gli ordini del customer
     */
    @Caching
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrders(String customerId) {
        return orderRepo.findByCustomer_IdOrderByDateDesc(customerId).stream()
                .map(o -> toDto(o, o.getStatus().name()))
                .collect(Collectors.toList());
    }

    /**
     * Restituisce un ordine specifico (solo se appartiene al customer)
     */
    @Caching
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId, String customerId) {
        Orders order = orderRepo.findByIdAndCustomer_Id(orderId, customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ordine non trovato: " + orderId));
        return toDto(order, order.getStatus().name());
    }

    // ——— Helper privati ———

    /**
     * Stub di pagamento inline: approva sempre.
     */
    // Helper per il pagamento (stub)
    private PaymentResult processPaymentStub(Long orderId, PaymentInfoDTO info) {
        return new PaymentResult(true, "APPROVED");
    }

    /**
     * Mappa l'entità Orders (con items) nel DTO di risposta
     */
    // Mapper di Orders → OrderDTO (rimane invariato)
    private OrderDTO toDto(Orders order, String paymentStatus) {
        List<OrderItemDetailDTO> items = order.getItems().stream()
                .map(i -> {
                    ProductVariant v = i.getVariant();
                    Color color      = v.getColor();

                    return OrderItemDetailDTO.builder()
                            .productId(i.getProduct().getId())
                            .productName(i.getProduct().getName())
                            .quantity(i.getQuantity())
                            .unitPrice(i.getUnitPrice())
                            .colorName(color.getName())
                            .colorHex(color.getHexCode())
                            .variantStockQuantity(v.getStockQuantity())
                            .build();
                })
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .date(order.getDate())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(items)
                .paymentStatus(paymentStatus)
                .build();
    }


    /**
     * Piccola classe interna per rappresentare l’esito del pagamento
     */
    // Piccola classe interna per rappresentare l’esito del pagamento
    private static class PaymentResult {
        final boolean approved;
        final String message;
        PaymentResult(boolean approved, String message) {
            this.approved = approved;
            this.message = message;
        }
    }
}
