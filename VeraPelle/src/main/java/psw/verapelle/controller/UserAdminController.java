package psw.verapelle.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import psw.verapelle.DTO.CustomerDTO;
import psw.verapelle.service.CustomerService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/auth/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    @Autowired
    private CustomerService customerService;

    /**
     * Lista tutti gli utenti.
     */
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllUsers() {
        List<CustomerDTO> users = customerService.getAllCustomers();
        return ResponseEntity.ok(users);
    }

    /**
     * Restituisce un utente per ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getUserById(@PathVariable String id) {
        try {
            CustomerDTO dto = customerService.getCustomerById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Aggiorna un utente esistente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateUser(
            @PathVariable String id,
            @Valid @RequestBody CustomerDTO dto) {
        try {
            CustomerDTO updated = customerService.updateCustomerById(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Elimina un utente.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        try {
            customerService.deleteCustomerById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
