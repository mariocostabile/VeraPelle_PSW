package psw.verapelle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import psw.verapelle.DTO.*;
import psw.verapelle.entity.Cart;
import psw.verapelle.entity.CartItem;
import psw.verapelle.entity.ProductImage;
import psw.verapelle.service.CartItemService;
import psw.verapelle.service.CartService;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemService cartItemService;

    @GetMapping
    public ResponseEntity<CartDTO> getCart(
            @CookieValue(value = "cartId", required = false) String cartIdCookie,
            @AuthenticationPrincipal Jwt principal       // ← inietta qui il token
    ) {
        Cart cart;
        ResponseCookie cookie;

        if (principal != null) {
            // Utente loggato: prendo il cart dal database via email/sub
            String email = principal.getClaimAsString("email");
            cart = cartService.getCartByCustomer(email);

            // Elimino il cookie guest (non serve più)
            cookie = ResponseCookie.from("cartId", "")
                    .path("/")
                    .maxAge(0)
                    .build();
        } else {
            // Guest: logica esistente basata sul cookie
            cart = cartService.getCart(cartIdCookie);
            cookie = ResponseCookie.from("cartId", cart.getId().toString())
                    .path("/")
                    .httpOnly(true)
                    .sameSite("Lax")
                    .maxAge(Duration.ofDays(7))
                    .build();
        }

        CartDTO dto = toCartDTO(cart);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(dto);
    }

    // 2) Aggiunge un item, gestendo guest-via-cookie e logged-in-via-JWT
    @PostMapping("/items")
    public ResponseEntity<CartItemDTO> addItem(
            @CookieValue(value = "cartId", required = false) String cartIdCookie,
            @AuthenticationPrincipal Jwt principal,      // ← aggiunto
            @RequestBody AddCartItemRequest req
    ) {
        CartItem ci;
        ResponseCookie cookie;

        if (principal != null) {
            // ——— UTENTE LOGGATO ———
            String email = principal.getClaimAsString("email");
            ci = cartItemService.addCartItemToCustomerCart(
                    email,
                    req.getProductId(),
                    req.getColorId(),
                    req.getQuantity()
            );
            // invalido il cookie guest
            cookie = ResponseCookie.from("cartId", "")
                    .path("/")
                    .maxAge(0)
                    .build();
        } else {
            // ——— UTENTE GUEST ——— (la tua logica originale)
            ci = cartItemService.addCartItem(
                    cartIdCookie,
                    req.getProductId(),
                    req.getColorId(),
                    req.getQuantity()
            );
            cookie = ResponseCookie.from("cartId", ci.getCart().getId().toString())
                    .path("/")
                    .httpOnly(true)
                    .sameSite("Lax")
                    .maxAge(Duration.ofDays(7))
                    .build();
        }

        CartItemDTO dto = toCartItemDTO(ci);
        return ResponseEntity.status(201)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(dto);
    }

    // 3) Modifica la quantità, ora guest‐cookie o logged‐in‐JWT
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartItemDTO> updateItem(
            @CookieValue(value = "cartId", required = false) String cartIdCookie,
            @AuthenticationPrincipal Jwt principal,            // ← aggiunto
            @PathVariable Long itemId,
            @RequestBody UpdateCartItemQuantityRequest req
    ) {
        CartItem ci;
        if (principal != null) {
            // ——— UTENTE LOGGATO ———
            String email = principal.getClaimAsString("email");
            ci = cartItemService.updateCartItemQuantityForCustomerCart(
                    email, itemId, req.getQuantity()
            );
        } else {
            // ——— GUEST ———
            ci = cartItemService.updateCartItemQuantity(
                    cartIdCookie, itemId, req.getQuantity()
            );
        }
        return ResponseEntity.ok(toCartItemDTO(ci));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @CookieValue(value = "cartId", required = false) String cartIdCookie,
            @AuthenticationPrincipal Jwt principal,      // ← aggiunto
            @PathVariable Long itemId
    ) {
        if (principal != null) {
            // UTENTE LOGGATO: delego al service specifico
            String email = principal.getClaimAsString("email");
            cartItemService.removeCartItemFromCustomerCart(email, itemId);
        } else {
            // GUEST: logica cookie-based
            cartItemService.removeCartItem(cartIdCookie, itemId);
        }
        return ResponseEntity.noContent().build();
    }


    // 5) Svuota tutto il carrello, guest‐cookie o logged‐in‐JWT
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @CookieValue(value = "cartId", required = false) String cartIdCookie,
            @AuthenticationPrincipal Jwt principal    // ← aggiunto
    ) {
        if (principal != null) {
            // ——— UTENTE LOGGATO ———
            cartService.clearCartByCustomer();
        } else {
            // ——— GUEST ———
            cartService.clearCart(cartIdCookie);
        }
        return ResponseEntity.noContent().build();
    }


    // --- helper per trasformare entity → DTO ---

    private CartDTO toCartDTO(Cart cart) {
        List<CartItemDTO> items = cart.getCartItems().stream()
                .map(this::toCartItemDTO)
                .collect(Collectors.toList());
        double total = items.stream()
                .mapToDouble(CartItemDTO::getSubtotal)
                .sum();
        return new CartDTO(cart.getId(), items, total);
    }

    private CartItemDTO toCartItemDTO(CartItem ci) {
        var p = ci.getProduct();
        // estraiamo gli ID di categorie e colori
        List<Long> categoryIds = p.getCategories().stream()
                .map(c -> c.getId())
                .collect(Collectors.toList());
        List<Long> colorIds = p.getColors().stream()
                .map(c -> c.getId())
                .collect(Collectors.toList());

        // estraiamo gli URL delle immagini
        List<String> imageUrls = p.getImages().stream()
                .map(ProductImage::getUrlPath)
                .collect(Collectors.toList());

        var prodDto = new ProductDTO(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStockQuantity(),
                categoryIds,
                colorIds
        );

        var color = ci.getSelectedColor();
        var colorDto = new ColorDTO(
                color.getId(),
                color.getName(),
                color.getHexCode()
        );

        // miniatura: prima immagine se presente
        String thumb = imageUrls.isEmpty() ? null : imageUrls.get(0);

        return new CartItemDTO(
                ci.getId(),
                prodDto,
                ci.getQuantity(),
                prodDto.getPrice().doubleValue() * ci.getQuantity(),
                thumb,
                colorDto
        );
    }

    /**
     * Chiamalo dal front-end subito dopo il login per fondere il guest cart
     * e tornare il nuovo cart via DTO + settare un cookie vuoto per guest.
     */
    @PostMapping("/merge")
    public ResponseEntity<CartDTO> mergeCart(
            @CookieValue(value = "cartId", required = false) String cartIdCookie,
            @AuthenticationPrincipal Jwt principal        // ← inietta qui il token decodificato
    ) {
        // Estrai l’email (o il subject) dal JWT
        String email = principal.getClaimAsString("email");
        // oppure, se preferisci usare il subject:
        // String email = principal.getSubject();

        // Fonde i carrelli passando la email
        Cart merged = cartService.mergeGuestCartIntoCustomerCart(cartIdCookie, email);

        // Invalida il cookie guest
        ResponseCookie deleteCookie = ResponseCookie.from("cartId", "")
                .path("/")
                .maxAge(0)
                .build();

        CartDTO dto = toCartDTO(merged);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(dto);
    }

}
