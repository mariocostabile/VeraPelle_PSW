package psw.verapelle.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psw.verapelle.entity.Cart;
import psw.verapelle.entity.CartItem;
import psw.verapelle.entity.Color;
import psw.verapelle.entity.Product;
import psw.verapelle.entity.ProductVariant;
import psw.verapelle.repository.CartItemRepository;
import psw.verapelle.repository.CartRepository;
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

    @Autowired
    private CartRepository cartRepository;

    @PersistenceContext
    private EntityManager em;


    /**
     * Aggiunge un item al carrello (guest o utente loggato).
     * Al termine, forza l’incremento di cart.version con OPTIMISTIC_FORCE_INCREMENT.
     */
    @Transactional
    public CartItem addCartItem(String cartIdCookie, Long productId, Long colorId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        // 1) Carico il carrello nello stesso contesto TX (Cart sarà managed)
        Cart cart = cartService.getCart(cartIdCookie);

        // 2) Carico prodotto e colore
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Color color = colorRepository.findById(colorId)
                .orElseThrow(() -> new RuntimeException("Color not found"));

        // 3) Controllo dello stock della variante colore
        ProductVariant variant = variantRepository
                .findByProductIdAndColorId(productId, colorId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        int existingQty = cart.getCartItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(productId)
                        && ci.getSelectedColor().getId().equals(colorId))
                .mapToInt(CartItem::getQuantity)
                .sum();

        if (existingQty + quantity > variant.getStockQuantity()) {
            throw new RuntimeException("Insufficient stock for color "
                    + color.getName() + ": only "
                    + (variant.getStockQuantity() - existingQty)
                    + " left");
        }

        // 4) Cerco un CartItem identico già esistente
        Optional<CartItem> existing = cart.getCartItems().stream()
                .filter(ci ->
                        ci.getProduct().getId().equals(productId) &&
                                ci.getSelectedColor().getId().equals(colorId)
                )
                .findFirst();

        CartItem ci;
        if (existing.isPresent()) {
            // 4a) Se esiste, aggiorno quantità e salvo il CartItem
            ci = existing.get();
            ci.setQuantity(ci.getQuantity() + quantity);
            ci = cartItemRepository.save(ci);
        } else {
            // 4b) Altrimenti creo un nuovo CartItem e lo salvo
            ci = new CartItem();
            ci.setCart(cart);
            ci.setProduct(product);
            ci.setSelectedColor(color);
            ci.setQuantity(quantity);
            ci = cartItemRepository.save(ci);

            // Aggiungo alla lista del parent
            cart.getCartItems().add(ci);
        }

        // 5) Forzo l’incremento di cart.version via lock ottimistico
        em.lock(cart, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        return ci;
    }


    /**
     * Aggiorna la quantità di un CartItem esistente (guest).
     * Al termine, forza l’incremento di cart.version.
     */
    @Transactional
    public CartItem updateCartItemQuantity(String cartIdCookie, Long itemId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        // 1) Recupero il carrello guest (managed nella stessa TX)
        Cart cart = cartService.getCart(cartIdCookie);

        // 2) Carico il CartItem e ne verifico l’appartenenza
        CartItem ci = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));
        if (!ci.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("CartItem does not belong to this cart");
        }

        // 3) Controllo stock in base alla nuova quantità
        Long prodId  = ci.getProduct().getId();
        Long colorId = ci.getSelectedColor().getId();
        ProductVariant variant = variantRepository
                .findByProductIdAndColorId(prodId, colorId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        int otherQty = cart.getCartItems().stream()
                .filter(x ->
                        x.getProduct().getId().equals(prodId) &&
                                x.getSelectedColor().getId().equals(colorId) &&
                                !x.getId().equals(ci.getId())
                )
                .mapToInt(CartItem::getQuantity)
                .sum();

        if (otherQty + quantity > variant.getStockQuantity()) {
            throw new RuntimeException("Insufficient stock for color "
                    + variant.getColor().getName() + ": only "
                    + (variant.getStockQuantity() - otherQty)
                    + " left");
        }

        // 4) Applico la modifica e salvo il CartItem
        ci.setQuantity(quantity);
        CartItem updated = cartItemRepository.save(ci);

        // 5) Forzo l’incremento di cart.version
        em.lock(cart, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        return updated;
    }


    /**
     * Aggiorna la quantità di un CartItem del carrello utente loggato.
     * Al termine, forza l’incremento di cart.version.
     */
    @Transactional
    public CartItem updateCartItemQuantityForCustomerCart(
            String customerEmail,
            Long itemId,
            int quantity
    ) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        // 1) Prendo il carrello del customer (managed nella stessa TX)
        Cart cart = cartService.getCartByCustomer(customerEmail);

        // 2) Carico il CartItem e ne verifico l’appartenenza
        CartItem ci = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));
        if (!ci.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("CartItem does not belong to this cart");
        }

        // 3) Controllo stock in base alla nuova quantità
        Long prodId  = ci.getProduct().getId();
        Long colorId = ci.getSelectedColor().getId();
        ProductVariant variant = variantRepository
                .findByProductIdAndColorId(prodId, colorId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        int otherQty = cart.getCartItems().stream()
                .filter(x ->
                        x.getProduct().getId().equals(prodId) &&
                                x.getSelectedColor().getId().equals(colorId) &&
                                !x.getId().equals(ci.getId())
                )
                .mapToInt(CartItem::getQuantity)
                .sum();

        if (otherQty + quantity > variant.getStockQuantity()) {
            throw new RuntimeException("Insufficient stock for color "
                    + variant.getColor().getName() + ": only "
                    + (variant.getStockQuantity() - otherQty)
                    + " left");
        }

        // 4) Applico la modifica e salvo il CartItem
        ci.setQuantity(quantity);
        CartItem updated = cartItemRepository.save(ci);

        // 5) Forzo l’incremento di cart.version
        em.lock(cart, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        return updated;
    }


    /**
     * Rimuove un CartItem dal carrello guest.
     * Al termine, forza l’incremento di cart.version.
     */
    @Transactional
    public void removeCartItem(String cartIdCookie, Long cartItemId) {
        // 1) Prendo il carrello guest
        Cart cart = cartService.getCart(cartIdCookie);

        // 2) Carico il CartItem e ne verifico l’appartenenza
        CartItem ci = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));
        if (!ci.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("CartItem does not belong to this cart");
        }

        // 3) Elimino il CartItem e lo rimuovo dalla lista del cart
        cartItemRepository.delete(ci);
        cart.getCartItems().removeIf(x -> x.getId().equals(cartItemId));

        // 4) Forzo l’incremento di cart.version
        em.lock(cart, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }


    /**
     * Rimuove un CartItem dal carrello utente loggato.
     * Al termine, forza l’incremento di cart.version.
     */
    @Transactional
    public void removeCartItemFromCustomerCart(String customerEmail, Long itemId) {
        // 1) Prendo il carrello del customer
        Cart cart = cartService.getCartByCustomer(customerEmail);

        // 2) Carico il CartItem e ne verifico l’appartenenza
        CartItem ci = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));
        if (!ci.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("CartItem does not belong to this cart");
        }

        // 3) Elimino il CartItem e lo rimuovo dalla lista del cart
        cartItemRepository.delete(ci);
        cart.getCartItems().removeIf(x -> x.getId().equals(itemId));

        // 4) Forzo l’incremento di cart.version
        em.lock(cart, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }


    /**
     * Aggiunge un item al carrello dell’utente loggato.
     * Al termine, forza l’incremento di cart.version.
     */
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

        // 1) Prendo o creo il carrello del customer (managed nella stessa TX)
        Cart customerCart = cartService.getCartByCustomer(customerEmail);

        // 2) Controllo dello stock della variante colore
        ProductVariant variant = variantRepository
                .findByProductIdAndColorId(productId, colorId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        int existingQty = customerCart.getCartItems().stream()
                .filter(ci ->
                        ci.getProduct().getId().equals(productId) &&
                                ci.getSelectedColor().getId().equals(colorId)
                )
                .mapToInt(CartItem::getQuantity)
                .sum();

        if (existingQty + quantity > variant.getStockQuantity()) {
            throw new RuntimeException("Insufficient stock for color "
                    + variant.getColor().getName() + ": only "
                    + (variant.getStockQuantity() - existingQty)
                    + " left");
        }

        // 3) Cerco un CartItem identico già esistente
        Optional<CartItem> existing = customerCart.getCartItems().stream()
                .filter(ci ->
                        ci.getProduct().getId().equals(productId) &&
                                ci.getSelectedColor().getId().equals(colorId)
                )
                .findFirst();

        CartItem ci;
        if (existing.isPresent()) {
            // 3a) Se esiste, aggiorno quantità e salvo
            ci = existing.get();
            ci.setQuantity(ci.getQuantity() + quantity);
            ci = cartItemRepository.save(ci);
        } else {
            // 3b) Altrimenti creo un nuovo CartItem e lo salvo
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
            ci = cartItemRepository.save(ci);
            customerCart.getCartItems().add(ci);
        }

        // 4) Forzo l’incremento di customerCart.version
        em.lock(customerCart, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        return ci;
    }
}
