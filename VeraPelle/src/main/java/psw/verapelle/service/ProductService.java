package psw.verapelle.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psw.verapelle.DTO.ProductDTO;
import psw.verapelle.entity.Category;
import psw.verapelle.entity.Product;
import psw.verapelle.repository.CategoryRepository;
import psw.verapelle.repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public Product createProduct(ProductDTO productDTO) {
        // Validazione del nome
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }

        // Costruzione dell'entità Product
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());

        // Associazione delle categorie (Many-to-Many)
        List<Category> categories = productDTO.getCategoryIds().stream()
                .map(id -> categoryRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Category not found: " + id)))
                .collect(Collectors.toList());
        product.setCategories(categories);

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductDTO productDTO) {
        return productRepository.findById(id)
                .map(product -> {
                    // Aggiornamento campi base
                    if (productDTO.getName() != null) {
                        product.setName(productDTO.getName());
                    }
                    if (productDTO.getDescription() != null) {
                        product.setDescription(productDTO.getDescription());
                    }
                    if (productDTO.getPrice() != null) {
                        product.setPrice(productDTO.getPrice());
                    }
                    if (productDTO.getStockQuantity() != 0) {
                        product.setStockQuantity(productDTO.getStockQuantity());
                    }

                    // Riassegnazione categorie
                    if (productDTO.getCategoryIds() != null) {
                        List<Category> categories = productDTO.getCategoryIds().stream()
                                .map(cid -> categoryRepository.findById(cid)
                                        .orElseThrow(() -> new RuntimeException("Category not found: " + cid)))
                                .collect(Collectors.toList());
                        product.setCategories(categories);
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

    @Transactional
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }
}
