package psw.verapelle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import psw.verapelle.DTO.ColorDTO;
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

    @GetMapping
    public Page<ProductPublicDTO> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, name = "categories") List<Long> categoryIds
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<Product> products = (categoryIds != null && !categoryIds.isEmpty())
                ? productService.findAllByCategories(categoryIds, pageable)
                : productService.findAll(Optional.empty(), pageable);

        return products.map(this::toPublicDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductPublicDTO> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(toPublicDTO(product));
    }

    /**
     * Suggestion: ricerca prodotti per nome (ignore case)
     */
    @GetMapping("/suggestions")
    public List<ProductPublicDTO> suggestProducts(@RequestParam("q") String keyword) {
        return productService.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::toPublicDTO)
                .collect(Collectors.toList());
    }

    private ProductPublicDTO toPublicDTO(Product product) {
        String baseUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString();

        List<String> imageUrls = product.getImages().stream()
                .map(pi -> baseUrl + pi.getUrlPath())
                .collect(Collectors.toList());

        List<String> categories = product.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toList());

        List<ColorDTO> colors = product.getColors().stream()
                .map(c -> new ColorDTO(c.getId(), c.getName(), c.getHexCode()))
                .collect(Collectors.toList());

        return new ProductPublicDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                categories,
                product.getDescription(),
                imageUrls,
                colors,
                product.getStockQuantity()
        );
    }
}
