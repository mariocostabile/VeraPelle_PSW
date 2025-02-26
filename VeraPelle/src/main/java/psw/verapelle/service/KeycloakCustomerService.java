//package psw.verapelle.service;
//
//import jakarta.transaction.Transactional;
//import jakarta.ws.rs.core.Response;
//import org.keycloak.admin.client.Keycloak;
//import org.keycloak.admin.client.resource.UsersResource;
//import org.keycloak.representations.idm.CredentialRepresentation;
//import org.keycloak.representations.idm.UserRepresentation;
//import org.springframework.stereotype.Service;
//import psw.verapelle.entity.Customer;
//import psw.verapelle.repository.CustomerRepository;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class KeycloakCustomerService {
//
//    private final Keycloak keycloak;
//    private final CustomerRepository customerRepository;
//    private final String realm = "pelle-vera-api";
//
//    public KeycloakCustomerService(Keycloak keycloak, CustomerRepository customerRepository) {
//        this.keycloak = keycloak;
//        this.customerRepository = customerRepository;
//    }
//
//    @Transactional
//    public String createCustomer(String username, String email, String password) {
//        UsersResource usersResource = keycloak.realm(realm).users();
//
//        UserRepresentation user = new UserRepresentation();
//        user.setUsername(username);
//        user.setEmail(email);
//        user.setEnabled(true);
//
//        CredentialRepresentation credential = new CredentialRepresentation();
//        credential.setTemporary(false);
//        credential.setType(CredentialRepresentation.PASSWORD);
//        credential.setValue(password);
//        user.setCredentials(Collections.singletonList(credential));
//
//        Response response = usersResource.create(user);
//        if (response.getStatus() == 201) {
//            List<UserRepresentation> users = usersResource.search(username);
//            if (!users.isEmpty()) {
//                String keycloakId = users.get(0).getId();
//                Customer customer = new Customer();
//                customer.setId(keycloakId);
//                customer.setUsername(username);
//                customer.setEmail(email);
//                customerRepository.save(customer);
//                return "Customer creato e sincronizzato con DB!";
//            }
//        }
//        return "Errore nella creazione: " + response.getStatus();
//    }
//
//    @Transactional
//    public String deleteCustomer(String username) {
//        String userId = getUserIdByUsername(username);
//        if (userId == null) {
//            return "Customer non trovato!";
//        }
//        keycloak.realm(realm).users().get(userId).remove();
//        customerRepository.deleteById(userId);
//        return "Customer eliminato con successo!";
//    }
//
//    @Transactional
//    public String updateCustomerEmail(String username, String newEmail) {
//        String userId = getUserIdByUsername(username);
//        if (userId == null) {
//            return "Customer non trovato!";
//        }
//        UsersResource usersResource = keycloak.realm(realm).users();
//        UserRepresentation user = usersResource.get(userId).toRepresentation();
//        user.setEmail(newEmail);
//        usersResource.get(userId).update(user);
//        Optional<Customer> customerOptional = customerRepository.findById(userId);
//        if (customerOptional.isPresent()) {
//            Customer customer = customerOptional.get();
//            customer.setEmail(newEmail);
//            customerRepository.save(customer);
//        }
//        return "Email aggiornata con successo!";
//    }
//
//    public UserRepresentation getCustomerInfo(String username) {
//        String userId = getUserIdByUsername(username);
//        if (userId == null) {
//            throw new RuntimeException("Customer non trovato!");
//        }
//        return keycloak.realm(realm).users().get(userId).toRepresentation();
//    }
//
//    public String resetCustomerPassword(String username, String newPassword) {
//        String userId = getUserIdByUsername(username);
//        if (userId == null) {
//            return "Customer non trovato!";
//        }
//        CredentialRepresentation credential = new CredentialRepresentation();
//        credential.setTemporary(false);
//        credential.setType(CredentialRepresentation.PASSWORD);
//        credential.setValue(newPassword);
//        keycloak.realm(realm).users().get(userId).resetPassword(credential);
//        return "Password resettata con successo!";
//    }
//
//    private String getUserIdByUsername(String username) {
//        List<UserRepresentation> users = keycloak.realm(realm).users().search(username);
//        return users.isEmpty() ? null : users.get(0).getId();
//    }
//}