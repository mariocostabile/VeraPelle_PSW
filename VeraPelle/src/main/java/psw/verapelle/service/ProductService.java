package psw.verapelle.service;

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

    public Product createProduct(ProductDTO productDTO) {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Product product = new Product(null, productDTO.getName(), productDTO.getDescription(),
                productDTO.getPrice(), productDTO.getStockQuantity(), category);
        return productRepository.save(product);
    }
}
