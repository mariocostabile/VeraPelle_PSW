package psw.verapelle.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import psw.verapelle.DTO.CategoryDTO;
import psw.verapelle.DTO.CustomerDTO;
import psw.verapelle.entity.Customer;
import psw.verapelle.entity.Category;
import psw.verapelle.service.CustomerService;
import psw.verapelle.service.CategoryService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Registra l'utente nel database interno basandosi sul JWT di Keycloak.
     */
    @PostMapping("/auth/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> registerCustomer(@AuthenticationPrincipal Jwt principal) {
        String keycloakId = principal.getClaimAsString("sub");
        Optional<Customer> existing = customerService.findCustomerById(keycloakId);
        if (existing.isEmpty()) {
            Customer saved = customerService.saveCustomer(principal);
            CustomerDTO dto = customerService.getCustomerById(saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists.");
    }

    /**
     * Restituisce i dati dell'utente corrente.
     */
    @GetMapping("/auth/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomerDTO> getCurrentCustomer(@AuthenticationPrincipal Jwt principal) {
        String keycloakId = principal.getClaimAsString("sub");
        CustomerDTO dto = customerService.getCustomerById(keycloakId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Aggiorna i dati dell'utente corrente.
     */
    @PutMapping("/auth/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomerDTO> updateCurrentCustomer(
            @AuthenticationPrincipal Jwt principal,
            @Valid @RequestBody CustomerDTO dto) {

        CustomerDTO updated = customerService.updateCustomer(principal, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Endpoint pubblico per ottenere tutte le categorie.
     */
    @GetMapping("/categories")
    public List<CategoryDTO> getAllCategories() {
        return categoryService.getAllCategories().stream()
                .map(c -> {
                    CategoryDTO dto = new CategoryDTO();
                    dto.setId(c.getId());
                    dto.setName(c.getName());
                    dto.setDescription(c.getDescription());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
