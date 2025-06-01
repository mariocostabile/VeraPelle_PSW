// src/main/java/psw/verapelle/controller/ProductController.java
package psw.verapelle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import psw.verapelle.DTO.ProductDTO;
import psw.verapelle.entity.Color;
import psw.verapelle.entity.Category;
import psw.verapelle.entity.Product;
import psw.verapelle.entity.ProductVariant;
import psw.verapelle.service.ProductService;
import psw.verapelle.DTO.VariantDTO;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return productService.getAllProducts()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ProductDTO getProductById(@PathVariable Long id) {
        return toDTO(productService.getProductById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ProductDTO createProduct(@RequestBody ProductDTO productDTO) {
        Product created = productService.createProduct(productDTO);
        return toDTO(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ProductDTO updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDTO productDTO
    ) {
        Product updated = productService.updateProduct(id, productDTO);
        return toDTO(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    private ProductDTO toDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());

        // Calcolo del totale delle quantità come somma delle varianti
        int totalQty = p.getVariants().stream()
                .map(ProductVariant::getStockQuantity)       // Integer
                .filter(Objects::nonNull)             // escludi eventuali null
                .mapToInt(Integer::intValue)          // converti in int
                .sum();
        dto.setStockQuantity(totalQty);

        // Categoria IDs
        dto.setCategoryIds(
                p.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toList())
        );

        // Varianti colore con stock dedicato (null-safe)
        dto.setVariants(
                p.getVariants().stream()
                        .map(v -> {
                            Integer vQty = v.getStockQuantity();
                            return new VariantDTO(
                                    v.getColor().getId(),
                                    v.getColor().getName(),
                                    v.getColor().getHexCode(),
                                    vQty != null ? vQty : 0
                            );
                        })
                        .collect(Collectors.toList())
        );

        return dto;
    }

}
