package psw.verapelle.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psw.verapelle.DTO.ProductDTO;
import psw.verapelle.entity.Color;
import psw.verapelle.entity.Product;
import psw.verapelle.repository.CategoryRepository;
import psw.verapelle.repository.ColorRepository;
import psw.verapelle.repository.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ColorRepository colorRepository;  // ← iniettato

    @Transactional
    public Product createProduct(ProductDTO productDTO) {
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());

        // categorie
        product.setCategories(
                productDTO.getCategoryIds().stream()
                        .map(id -> categoryRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Category not found: " + id)))
                        .collect(Collectors.toList())
        );

        // colori (se presenti)
        if (productDTO.getColorIds() != null) {
            product.setColors(
                    productDTO.getColorIds().stream()
                            .map(id -> colorRepository.findById(id)
                                    .orElseThrow(() -> new RuntimeException("Color not found: " + id)))
                            .collect(Collectors.toList())
            );
        }

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductDTO productDTO) {
        return productRepository.findById(id)
                .map(product -> {
                    if (productDTO.getName() != null) {
                        product.setName(productDTO.getName());
                    }
                    if (productDTO.getDescription() != null) {
                        product.setDescription(productDTO.getDescription());
                    }
                    if (productDTO.getPrice() != null) {
                        product.setPrice(productDTO.getPrice());
                    }
                    // StockQuantity è primitivo, aggiorniamo se >= 0
                    if (productDTO.getStockQuantity() >= 0) {
                        product.setStockQuantity(productDTO.getStockQuantity());
                    }

                    if (productDTO.getCategoryIds() != null) {
                        product.setCategories(
                                productDTO.getCategoryIds().stream()
                                        .map(cid -> categoryRepository.findById(cid)
                                                .orElseThrow(() -> new RuntimeException("Category not found: " + cid)))
                                        .collect(Collectors.toList())
                        );
                    }

                    // colori (se presenti)
                    if (productDTO.getColorIds() != null) {
                        product.setColors(
                                productDTO.getColorIds().stream()
                                        .map(cid -> colorRepository.findById(cid)
                                                .orElseThrow(() -> new RuntimeException("Color not found: " + cid)))
                                        .collect(Collectors.toList())
                        );
                    }

                    return productRepository.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Product> findAll(Optional<Long> categoryId, Pageable pageable) {
        return categoryId.map(cid -> productRepository.findAllByCategories_Id(cid, pageable))
                .orElseGet(() -> productRepository.findAll(pageable));
    }

    @Transactional(readOnly = true)
    public Page<Product> findAllByCategories(List<Long> categoryIds, Pageable pageable) {
        return (categoryIds != null && !categoryIds.isEmpty())
                ? productRepository.findByAllCategories(categoryIds, pageable)
                : productRepository.findAll(pageable);
    }
}
