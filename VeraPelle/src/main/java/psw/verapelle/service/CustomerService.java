package psw.verapelle.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import psw.verapelle.DTO.CustomerDTO;
import psw.verapelle.entity.Customer;
import psw.verapelle.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    /*-------------------------------------------------
     |               Self-service methods              |
     -------------------------------------------------*/

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(c -> new CustomerDTO(
                        c.getId(),
                        c.getFirstName(),
                        c.getLastName(),
                        c.getDateOfBirth(),
                        c.getAddress(),
                        c.getPhone(),
                        c.getEmail()
                ))
                .collect(Collectors.toList());
    }

    public Optional<Customer> findCustomerById(String id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Customer saveCustomer(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getClaimAsString("sub");
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");
        Customer customer = new Customer();
        customer.setId(keycloakId);
        customer.setEmail(email);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        return customerRepository.save(customer);
    }

    public Customer getCustomer(String keycloakId) {
        return customerRepository.findById(keycloakId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public CustomerDTO updateCustomer(Jwt jwt, CustomerDTO dto) {
        String keycloakId = jwt.getClaimAsString("sub");
        Customer c = customerRepository.findById(keycloakId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // patch solo i campi modificabili
        c.setPhone(dto.getPhone());
        c.setAddress(dto.getAddress());
        c.setDateOfBirth(dto.getDateOfBirth());

        Customer saved = customerRepository.save(c);

        // rimappa in DTO completo
        return new CustomerDTO(
                saved.getId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getDateOfBirth(),
                saved.getAddress(),
                saved.getPhone(),
                saved.getEmail()
        );
    }

    /*-------------------------------------------------
     |               Admin-service methods             |
     -------------------------------------------------*/

    @Transactional
    public CustomerDTO getCustomerById(String id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return new CustomerDTO(
                c.getId(),
                c.getFirstName(),
                c.getLastName(),
                c.getDateOfBirth(),
                c.getAddress(),
                c.getPhone(),
                c.getEmail()
        );
    }

    @Transactional
    public CustomerDTO updateCustomerById(String id, @Valid CustomerDTO dto) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // aggiorniamo tutti i campi dal DTO
        c.setFirstName(dto.getFirstName());
        c.setLastName(dto.getLastName());
        c.setDateOfBirth(dto.getDateOfBirth());
        c.setAddress(dto.getAddress());
        c.setPhone(dto.getPhone());
        c.setEmail(dto.getEmail());

        Customer saved = customerRepository.save(c);
        return new CustomerDTO(
                saved.getId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getDateOfBirth(),
                saved.getAddress(),
                saved.getPhone(),
                saved.getEmail()
        );
    }

    @Transactional
    public void deleteCustomerById(String id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customerRepository.delete(c);
    }
}
