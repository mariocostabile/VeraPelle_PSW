package psw.verapelle.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import psw.verapelle.DTO.CustomerDTO;
import psw.verapelle.entity.Customer;
import psw.verapelle.repository.CustomerRepository;
import psw.verapelle.service.CustomerService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/auth/getAllCustomer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        List<CustomerDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    //utile per admin panel
    @GetMapping("/auth/get/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Customer> getCustomerById(@PathVariable String id) {
        Optional<Customer> customer = customerService.findCustomerById(id);
        return customer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PutMapping("/auth/update/{id}")
    @PreAuthorize("hasRole('USER')")  //il service è lo stesso perché verifica sempre prima se presente, in questo caso l'utente deve però avere un ruolo user che si assegna solo se già autenticato
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDTO customerDTO) {
        return ResponseEntity.ok(customerService.updateCustomer(id,customerDTO));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> registerCustomer(@AuthenticationPrincipal Jwt principal) {
        String keycloakId = principal.getClaimAsString("sub");
        Optional<Customer> customerOpt = customerService.findCustomerById(keycloakId);
        if (customerOpt.isEmpty())
            return ResponseEntity.ok(customerService.saveCustomer(principal));
        return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists.");
    }
    
    @GetMapping("/auth/me")
    public ResponseEntity<CustomerDTO> me(@AuthenticationPrincipal Jwt principal) {
        String keycloakId = principal.getClaimAsString("sub");
        try {
            Customer customer = customerService.getCustomer(keycloakId);
            CustomerDTO customerDTO = new CustomerDTO();
            customerDTO.setFirstName(customer.getFirstName());
            customerDTO.setLastName(customer.getLastName());
            customerDTO.setDateOfBirth(customer.getDateOfBirth());
            customerDTO.setEmail(customer.getEmail());
            customerDTO.setPhone(customer.getPhone());
            customerDTO.setAddress(customer.getAddress());
            return ResponseEntity.ok(customerDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}