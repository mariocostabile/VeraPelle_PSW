package psw.verapelle.service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psw.verapelle.entity.*;
import psw.verapelle.repository.CartItemRepository;
import psw.verapelle.repository.ColorRepository;
import psw.verapelle.repository.ProductRepository;
import psw.verapelle.repository.ProductVariantRepository;

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

    @Autowired
    private ProductVariantRepository variantRepository;


    /**
     * Aggiunge un item al carrello guest o loggato, basato sul cartIdCookie.
     */
    @Transactional
    public CartItem addCartItem(String cartIdCookie, Long productId, Long colorId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        // 1) Carico carrello, prodotto e colore
        Cart cart = cartService.getCart(cartIdCookie);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Color color = colorRepository.findById(colorId)
                .orElseThrow(() -> new RuntimeException("Color not found"));

        // 2) Leggo lo stock della variante colore
        ProductVariant variant = variantRepository
                .findByProductIdAndColorId(productId, colorId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        // 3) Calcolo quantità già presente di questa variante nel carrello
        int existingQty = cart.getCartItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(productId)
                        && ci.getSelectedColor().getId().equals(colorId))
                .mapToInt(CartItem::getQuantity)
                .sum();

        // 4) Verifico che non superi lo stock della variante
        if (existingQty + quantity > variant.getStockQuantity()) {
            throw new RuntimeException("Insufficient stock for color "
                    + color.getName() + ": only "
                    + (variant.getStockQuantity() - existingQty)
                    + " left");
        }

        // 5) Cerco un item già esistente identico
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

        // 6) Altrimenti ne creo uno nuovo
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

        // 1) Recupera il carrello guest
        Cart cart = cartService.getCart(cartIdCookie);

        // 2) Carica il CartItem e verifica appartenenza
        CartItem ci = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));
        if (!ci.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("CartItem does not belong to this cart");
        }

        // 3) Leggi lo stock della variante colore
        Long prodId  = ci.getProduct().getId();
        Long colorId = ci.getSelectedColor().getId();
        ProductVariant variant = variantRepository
                .findByProductIdAndColorId(prodId, colorId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        // 4) Calcola la quantità "esterna" (tutte le altre occorrenze di questa variante)
        int otherQty = cart.getCartItems().stream()
                .filter(x ->
                        x.getProduct().getId().equals(prodId) &&
                                x.getSelectedColor().getId().equals(colorId) &&
                                !x.getId().equals(ci.getId())
                )
                .mapToInt(CartItem::getQuantity)
                .sum();

        // 5) Verifica che la nuova quantità non superi lo stock
        if (otherQty + quantity > variant.getStockQuantity()) {
            throw new RuntimeException("Insufficient stock for color "
                    + variant.getColor().getName() + ": only "
                    + (variant.getStockQuantity() - otherQty)
                    + " left");
        }

        // 6) Applica la modifica e salva
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
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        // 1) Prendi il carrello del customer
        Cart cart = cartService.getCartByCustomer(customerEmail);

        // 2) Carica il CartItem e verifica appartenenza
        CartItem ci = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));
        if (!ci.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("CartItem does not belong to this cart");
        }

        // 3) Leggi lo stock della variante colore
        Long prodId  = ci.getProduct().getId();
        Long colorId = ci.getSelectedColor().getId();
        ProductVariant variant = variantRepository
                .findByProductIdAndColorId(prodId, colorId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        // 4) Calcola la quantità "esterna" (tutte le altre occorrenze di questa variante)
        int otherQty = cart.getCartItems().stream()
                .filter(x ->
                        x.getProduct().getId().equals(prodId) &&
                                x.getSelectedColor().getId().equals(colorId) &&
                                !x.getId().equals(ci.getId())
                )
                .mapToInt(CartItem::getQuantity)
                .sum();

        // 5) Verifica che la nuova quantità non superi lo stock
        if (otherQty + quantity > variant.getStockQuantity()) {
            throw new RuntimeException("Insufficient stock for color "
                    + variant.getColor().getName() + ": only "
                    + (variant.getStockQuantity() - otherQty)
                    + " left");
        }

        // 6) Applica la modifica e salva
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
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        // 1) Prendo o creo il carrello del customer
        Cart customerCart = cartService.getCartByCustomer(customerEmail);

        // 2) Carico variante colore per lo stock
        ProductVariant variant = variantRepository
                .findByProductIdAndColorId(productId, colorId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        // 3) Calcolo quantità già presente di questa variante nel carrello
        int existingQty = customerCart.getCartItems().stream()
                .filter(ci ->
                        ci.getProduct().getId().equals(productId) &&
                                ci.getSelectedColor().getId().equals(colorId)
                )
                .mapToInt(CartItem::getQuantity)
                .sum();

        // 4) Verifico che non superi lo stock della variante
        if (existingQty + quantity > variant.getStockQuantity()) {
            throw new RuntimeException("Insufficient stock for color "
                    + variant.getColor().getName() + ": only "
                    + (variant.getStockQuantity() - existingQty)
                    + " left");
        }

        // 5) Controllo se esiste già un item identico
        Optional<CartItem> existing = customerCart.getCartItems().stream()
                .filter(ci ->
                        ci.getProduct().getId().equals(productId) &&
                                ci.getSelectedColor().getId().equals(colorId)
                )
                .findFirst();

        CartItem ci;
        if (existing.isPresent()) {
            ci = existing.get();
            ci.setQuantity(ci.getQuantity() + quantity);
        } else {
            // 6) Creo un nuovo CartItem
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

        // 7) Salvo e ritorno
        return cartItemRepository.save(ci);
    }

}
