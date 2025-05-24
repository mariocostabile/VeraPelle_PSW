package psw.verapelle.service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psw.verapelle.entity.Cart;
import psw.verapelle.entity.CartItem;
import psw.verapelle.entity.Color;
import psw.verapelle.entity.Product;
import psw.verapelle.repository.CartItemRepository;
import psw.verapelle.repository.ColorRepository;
import psw.verapelle.repository.ProductRepository;

import java.util.Optional;

@Service
public class CartItemService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ColorRepository colorRepository;

    /**
     * Aggiunge un item al carrello guest o loggato, basato sul cartIdCookie.
     */
    @Transactional
    public CartItem addCartItem(String cartIdCookie, Long productId, Long colorId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        Cart cart = cartService.getCart(cartIdCookie);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Color color = colorRepository.findById(colorId)
                .orElseThrow(() -> new RuntimeException("Color not found"));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        // Cerca un item con stesso prodotto+colore nel carrello corrente
        Optional<CartItem> existing = cart.getCartItems().stream()
                .filter(ci ->
                        ci.getProduct().getId().equals(productId) &&
                                ci.getSelectedColor().getId().equals(colorId)
                )
                .findFirst();

        if (existing.isPresent()) {
            CartItem ci = existing.get();
            ci.setQuantity(ci.getQuantity() + quantity);
            return cartItemRepository.save(ci);
        }

        // Altrimenti ne creo uno nuovo
        CartItem ci = new CartItem();
        ci.setCart(cart);
        ci.setProduct(product);
        ci.setSelectedColor(color);
        ci.setQuantity(quantity);
        return cartItemRepository.save(ci);
    }

    /**
     * Aggiorna la quantità di un CartItem esistente (deve restare ≥ 1)
     * solo se appartiene al cart identificato da cartIdCookie.
     */
    @Transactional
    public CartItem updateCartItemQuantity(String cartIdCookie, Long itemId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        Cart cart = cartService.getCart(cartIdCookie);
        CartItem ci = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        if (!ci.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("CartItem does not belong to this cart");
        }

        ci.setQuantity(quantity);
        return cartItemRepository.save(ci);
    }

    /** Aggiorna qty per utente loggato sul cart DB */
    @Transactional
    public CartItem updateCartItemQuantityForCustomerCart(
            String customerEmail,
            Long itemId,
            int quantity
    ) {
        // 1) prendi il cart del customer
        Cart cart = cartService.getCartByCustomer(customerEmail);

        // 2) carica il CartItem
        CartItem ci = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        // 3) verifica che appartenga a questo cart
        if (!ci.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("CartItem does not belong to this cart");
        }

        // 4) aggiorna e salva
        ci.setQuantity(quantity);
        return cartItemRepository.save(ci);
    }


    /**
     * Rimuove un CartItem dal carrello identificato da cartIdCookie.
     */
    @Transactional
    public void removeCartItem(String cartIdCookie, Long cartItemId) {
        Cart cart = cartService.getCart(cartIdCookie);
        CartItem ci = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        if (!ci.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("CartItem does not belong to this cart");
        }

        cartItemRepository.delete(ci);
    }

    /** Rimuove un item dal carrello del customer loggato */
    @Transactional
    public void removeCartItemFromCustomerCart(String customerEmail, Long itemId) {
        // 1) prendi il cart del customer
        Cart cart = cartService.getCartByCustomer(customerEmail);

        // 2) carica l’item
        CartItem ci = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        // 3) controlla che appartenga davvero al carrello del customer
        if (!ci.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("CartItem does not belong to this cart");
        }

        // 4) rimuovi
        cartItemRepository.delete(ci);
    }


    /** Modalità “utente loggato”: aggiunge (o somma) l’item nel DB del customer */
    @Transactional
    public CartItem addCartItemToCustomerCart(
            String customerEmail,
            Long productId,
            Long colorId,
            int quantity
    ) {
        // 1) prendi o crea il carrello del customer
        Cart customerCart = cartService.getCartByCustomer(customerEmail);

        // 2) verifica se esiste già un item identico
        Optional<CartItem> existing = customerCart.getCartItems().stream()
                .filter(ci ->
                        ci.getProduct().getId().equals(productId) &&
                                ci.getSelectedColor().getId().equals(colorId)
                )
                .findAny();

        CartItem ci;
        if (existing.isPresent()) {
            ci = existing.get();
            ci.setQuantity(ci.getQuantity() + quantity);
        } else {
            // 3) altrimenti crea un nuovo CartItem
            ci = new CartItem();
            ci.setCart(customerCart);
            ci.setProduct(
                    productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found"))
            );
            ci.setSelectedColor(
                    colorRepository.findById(colorId)
                            .orElseThrow(() -> new RuntimeException("Color not found"))
            );
            ci.setQuantity(quantity);
            customerCart.getCartItems().add(ci);
        }

        // 4) salva e ritorna
        return cartItemRepository.save(ci);
    }
}
