package psw.verapelle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import psw.verapelle.DTO.ProductPublicDTO;
import psw.verapelle.entity.Category;
import psw.verapelle.entity.Product;
import psw.verapelle.service.ProductService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class PublicProductController {

    @Autowired
    private ProductService productService;

    /**
     * GET /api/products
     * Parametri:
     *   - page (default 0)
     *   - size (default 10)
     *   - categories (opzionale, lista di ID)
     * Restituisce una pagina di ProductPublicDTO, filtrata per tutte le categorie se categories è presente.
     */
    @GetMapping
    public Page<ProductPublicDTO> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, name = "categories") List<Long> categoryIds
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<Product> products;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            products = productService.findAllByCategories(categoryIds, pageable);
        } else {
            products = productService.findAll(Optional.empty(), pageable);
        }
        return products.map(this::toPublicDTO);
    }

    /**
     * GET /api/products/{id}
     * Restituisce il dettaglio di un singolo prodotto.
     */
    @GetMapping("/{id}")
    public ProductPublicDTO getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return toPublicDTO(product);
    }

    /**
     * Mapper da Product → ProductPublicDTO
     */
    private ProductPublicDTO toPublicDTO(Product product) {
        return new ProductPublicDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCategories()
                        .stream()
                        .map(Category::getName)
                        .collect(Collectors.toList())
        );
    }
}
