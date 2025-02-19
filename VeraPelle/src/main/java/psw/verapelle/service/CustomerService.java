package psw.verapelle.service;

import org.springframework.beans.factory.annotation.Autowired;
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
                customer.getLastName(), customer.getDateOfBirth(), customer.getAddress(), customer.getEmail(), customer.getPhone())).collect(Collectors.toList());
    }

    public Optional<CustomerDTO> getCustomerById(Long id) {
        return customerRepository.findById(id).map(customer -> new CustomerDTO(customer.getId(), customer.getFirstName(),
                customer.getLastName(), customer.getDateOfBirth(), customer.getAddress(), customer.getEmail(), customer.getPhone()));
    }

    public Customer saveCustomer(CustomerDTO customerDTO) {
        Customer customer = new Customer(null, customerDTO.getFirstName(), customerDTO.getLastName(),
                customerDTO.getDateOfBirth(), customerDTO.getAddress(), customerDTO.getEmail(), customerDTO.getPhone(), null, null);
        return customerRepository.save(customer);
    }
}
