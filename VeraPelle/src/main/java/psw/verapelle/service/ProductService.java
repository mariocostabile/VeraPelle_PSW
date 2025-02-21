package psw.verapelle.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psw.verapelle.DTO.ProductDTO;
import psw.verapelle.entity.Category;
import psw.verapelle.entity.Product;
import psw.verapelle.repository.CategoryRepository;
import psw.verapelle.repository.ProductRepository;


@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public Product createProduct(ProductDTO productDTO) {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Product product = new Product(null, productDTO.getName(), productDTO.getDescription(),
                productDTO.getPrice(), productDTO.getStockQuantity(), category, null);
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id).map(product -> {
            product.setName(updatedProduct.getName());
            product.setDescription(updatedProduct.getDescription());
            product.setPrice(updatedProduct.getPrice());
            product.setStockQuantity(updatedProduct.getStockQuantity());
            product.setCategory(updatedProduct.getCategory());
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found"));
    }
}
