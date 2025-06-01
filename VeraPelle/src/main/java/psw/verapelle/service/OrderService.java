package psw.verapelle.service;

import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    @Autowired
    private ProductVariantRepository variantRepository;



    /**
     * Crea un nuovo ordine per il customer identificato da customerId,
     * con i dati di CreateOrderRequest (items + shippingAddress + paymentInfo).
     */
    public OrderDTO createOrder(CreateOrderRequest req, String customerId) {
        // 1) Recupera il customer
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer non trovato: " + customerId));

        // 2) Crea e salva subito l'ordine con status CREATED
        Orders order = Orders.builder()
                .customer(customer)
                .shippingAddress(req.getShippingAddress())
                .status(OrderStatus.CREATED)
                .build();
        order = orderRepo.save(order);

        // 3) Simula il pagamento
        PaymentResult payment = processPaymentStub(order.getId(), req.getPaymentInfo());

        if (payment.approved) {
            // 4a) Se APPROVED: aggiungi items e decurta stock
            BigDecimal total = BigDecimal.ZERO;
            for (OrderItemDTO itemDto : req.getItems()) {
                Long productId = itemDto.getProductId();
                Long colorId   = itemDto.getColorId();
                int qty        = itemDto.getQuantity();

                // 1) Leggi la variante colore per questo prodotto
                ProductVariant variant = variantRepository
                        .findByProductIdAndColorId(productId, colorId)
                        .orElseThrow(() -> new RuntimeException(
                                "Variant non trovata per product " + productId + " color " + colorId));

                // 2) Verifica disponibilità
                if (variant.getStockQuantity() < qty) {
                    throw new RuntimeException("Stock insufficiente per variante: "
                            + "product " + productId
                            + " color " + colorId
                            + ", rimangono solo " + variant.getStockQuantity());
                }

                // 3) Decurta lo stock della variante e salva
                variant.setStockQuantity(variant.getStockQuantity() - qty);
                variantRepository.save(variant);

                // 3.bis) Riallinea anche lo stock complessivo in Product
                Product product = productRepo.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product non trovato: " + productId));
                int totalStock = product.getVariants().stream()
                        .map(ProductVariant::getStockQuantity)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .sum();
                product.setStockQuantity(totalStock);
                productRepo.save(product);

                // 4) Crea l’OrderItem con variante
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

        } else {
            // 4b) Se DECLINED: mantieni stock intatto e marca l'ordine come DECLINED
            order.setStatus(OrderStatus.DECLINED);
        }

        // 5) Salva l'ordine finale (con items e stato aggiornato)
        order = orderRepo.save(order);

        // 6) Mappa e restituisci DTO
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
                .orElseThrow(() -> new RuntimeException("Ordine non trovato: " + orderId));
        return toDto(order, order.getStatus().name());
    }

    // ——— Helper privati ———

    /**
     * Stub di pagamento inline: approva sempre.
     */
    private PaymentResult processPaymentStub(Long orderId, PaymentInfoDTO info) {
        // qui potresti aggiungere logiche random o controlli di info.getCardNumber()
        return new PaymentResult(true, "APPROVED");
    }

    /**
     * Mappa l'entità Orders (con items) nel DTO di risposta
     */
    private OrderDTO toDto(Orders order, String paymentStatus) {
        List<OrderItemDetailDTO> items = order.getItems().stream()
                .map(i -> {
                    ProductVariant v = i.getVariant();            // prendi la variante
                    Color color      = v.getColor();

                    return OrderItemDetailDTO.builder()
                            .productId(i.getProduct().getId())
                            .productName(i.getProduct().getName())
                            .quantity(i.getQuantity())
                            .unitPrice(i.getUnitPrice())
                            .colorName(color.getName())               // nuovo
                            .colorHex(color.getHexCode())             // nuovo
                            .variantStockQuantity(v.getStockQuantity()) // nuovo
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
    private static class PaymentResult {
        final boolean approved;
        final String message;
        PaymentResult(boolean approved, String message) {
            this.approved = approved;
            this.message = message;
        }
    }
}
