package psw.verapelle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import psw.verapelle.DTO.ProductDTO;
import psw.verapelle.entity.Category;
import psw.verapelle.entity.Product;
import psw.verapelle.service.CategoryService;
import psw.verapelle.service.ProductService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return productService.getAllProducts().stream()
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
    public ProductDTO updateProduct(@PathVariable Long id,
                                    @RequestBody ProductDTO productDTO) {
        // Prepariamo un entity “parziale” coi campi da aggiornare
        Category category = categoryService.getCategoryById(productDTO.getCategoryId());
        Product toUpdate = new Product();
        toUpdate.setName(productDTO.getName());
        toUpdate.setDescription(productDTO.getDescription());
        toUpdate.setPrice(productDTO.getPrice());
        toUpdate.setStockQuantity(productDTO.getStockQuantity());
        toUpdate.setCategory(category);

        Product updated = productService.updateProduct(id, toUpdate);
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
        dto.setStockQuantity(p.getStockQuantity());
        dto.setCategoryId(p.getCategory().getId());
        return dto;
    }
}
