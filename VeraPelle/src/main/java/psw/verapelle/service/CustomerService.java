package psw.verapelle.service;

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

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream().map(customer -> new CustomerDTO(customer.getId(), customer.getFirstName(),
                customer.getLastName(), customer.getDateOfBirth(), customer.getAddress(), customer.getPhone(), customer.getEmail())).collect(Collectors.toList());
    }

    public Optional<Customer> findCustomerById(String id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Customer saveCustomer(@AuthenticationPrincipal Jwt jwt){
        String keycloakId = jwt.getClaimAsString("sub");
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");
        Customer customer = new Customer();
        customer = new Customer();
        customer.setId(keycloakId);
        customer.setEmail(email);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        return customerRepository.save(customer);
    }

    public Customer getCustomer(String keycloakId) {
        return customerRepository.findById(keycloakId).orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public Customer updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(customerDTO.getId());
        updatedCustomer.setFirstName(customerDTO.getFirstName());
        updatedCustomer.setLastName(customerDTO.getLastName());
        updatedCustomer.setDateOfBirth(customerDTO.getDateOfBirth());
        updatedCustomer.setAddress(customerDTO.getAddress());
        updatedCustomer.setPhone(customerDTO.getPhone());
        updatedCustomer.setEmail(customerDTO.getEmail());
        return customerRepository.save(updatedCustomer);
    }
}


