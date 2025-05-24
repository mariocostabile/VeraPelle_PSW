package psw.verapelle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import psw.verapelle.DTO.CreateOrderRequest;
import psw.verapelle.DTO.OrderDTO;
import psw.verapelle.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/auth/orders")
@PreAuthorize("hasRole('USER')")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Crea un nuovo ordine. Richiede autenticazione.
     */
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @RequestBody CreateOrderRequest req,
            @AuthenticationPrincipal Jwt principal
    ) {
        String customerId = principal.getSubject();
        OrderDTO dto = orderService.createOrder(req, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Ritorna la lista di tutti gli ordini dell'utente autenticato.
     */
    @GetMapping
    public List<OrderDTO> getOrders(
            @AuthenticationPrincipal Jwt principal
    ) {
        String customerId = principal.getSubject();
        return orderService.getOrders(customerId);
    }

    /**
     * Ritorna il dettaglio di un singolo ordine dell'utente.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt principal
    ) {
        String customerId = principal.getSubject();
        OrderDTO dto = orderService.getOrderById(orderId, customerId);
        return ResponseEntity.ok(dto);
    }
}
