package psw.verapelle.service;

import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import psw.verapelle.entity.Cart;
import psw.verapelle.entity.CartItem;
import psw.verapelle.entity.Customer;
import psw.verapelle.repository.CartRepository;
import psw.verapelle.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Recupera il carrello per un utente guest, in base al cookie cartId.
     * Se il cookie manca o è malformato o non trova il cart esistente, ne crea uno nuovo.
     */
    @Caching
    @Transactional
    public Cart getCart(String cartIdCookie) {
        if (cartIdCookie != null) {
            try {
                Long cartId = Long.valueOf(cartIdCookie);
                return cartRepository.findById(cartId)
                        .orElseGet(this::createGuestCart);
            } catch (NumberFormatException e) {
                // cookie malformato: creiamo comunque un nuovo carrello
                return createGuestCart();
            }
        }
        // nessun cookie: nuovo guest cart
        return createGuestCart();
    }

    /**
     * Recupera il carrello per l'utente autenticato, creandolo se non esiste.
     * Questo metodo è usato in fase di checkout.
     */
    @Caching
    @Transactional
    public Cart getCartByCustomer() {
        String email = getAuthenticatedUserEmail();
        return getCartByCustomer(email);
    }

    /**
     * Recupera o crea il carrello associato al customer identificato dall'email.
     */
    @Caching
    @Transactional
    public Cart getCartByCustomer(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> createCart(customer));
    }

    /**
     * Crea un nuovo carrello guest (customer = null).
     */
    @Transactional
    protected Cart createGuestCart() {
        Cart cart = new Cart();
        // customer rimane null
        return cartRepository.save(cart);
    }

    /**
     * Crea un carrello associato a un customer loggato.
     */
    @Transactional
    protected Cart createCart(Customer customer) {
        Cart cart = new Cart();
        cart.setCustomer(customer);
        return cartRepository.save(cart);
    }

    /**
     * Svuota completamente il carrello guest identificato dal cookie.
     */
    @Transactional
    public void clearCart(String cartIdCookie) {
        Cart cart = getCart(cartIdCookie);
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    /**
     * Svuota completamente il carrello dell'utente autenticato.
     */
    @Transactional
    public void clearCartByCustomer() {
        Cart cart = getCartByCustomer();
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    /**
     * Estrae l'email dell'utente dal JWT.
     */
    private String getAuthenticatedUserEmail() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return jwt.getClaim("email");
    }

    /**
     * Fonde il carrello guest (cartIdCookie) dentro quello del customer autenticato.
     * Ritorna il CartDTO aggiornato e invalidа il guest cart.
     */
    @Transactional
    public Cart mergeGuestCartIntoCustomerCart(String cartIdCookie, String email) {
        // 1) Recupera il guest cart
        Cart guestCart = getCart(cartIdCookie);

        // 2) Recupera o crea il cart del customer identificato dall'email
        Cart customerCart = getCartByCustomer(email);

        // 3) Fonde ciascun CartItem del guest nel customer cart
        for (CartItem guestItem : List.copyOf(guestCart.getCartItems())) {
            Long prodId  = guestItem.getProduct().getId();
            Long colorId = guestItem.getSelectedColor().getId();     // <- qui

            Optional<CartItem> existing = customerCart.getCartItems().stream()
                    .filter(ci ->
                            ci.getProduct().getId().equals(prodId) &&
                                    ci.getSelectedColor().getId().equals(colorId)   // <- e qui
                    )
                    .findAny();

            if (existing.isPresent()) {
                // se esiste, somma le quantità
                CartItem ci = existing.get();
                ci.setQuantity(ci.getQuantity() + guestItem.getQuantity());
            } else {
                // altrimenti sposta l’item nel customerCart
                guestItem.setCart(customerCart);
                customerCart.getCartItems().add(guestItem);
            }
        }

        // 4) Svuota il carrello guest e salva
        guestCart.getCartItems().clear();
        cartRepository.save(guestCart);

        // 5) Salva e restituisci il cart del customer
        return cartRepository.save(customerCart);
    }



}
