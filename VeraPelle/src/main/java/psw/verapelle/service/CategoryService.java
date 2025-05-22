package psw.verapelle.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psw.verapelle.DTO.CategoryDTO;
import psw.verapelle.entity.Category;
import psw.verapelle.entity.Product;
import psw.verapelle.repository.CategoryRepository;
import psw.verapelle.repository.ProductRepository;

import java.util.List;
import java.util.Objects;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Aggiungi il ProductRepository
    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Category createCategory(CategoryDTO categoryDTO) {
        if (categoryDTO.getName() == null || categoryDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        Category category = new Category(
                null,
                categoryDTO.getName(),
                categoryDTO.getDescription(),
                null,
                null
        );
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // 1) “Stacca” la categoria da tutti i prodotti
        List<Product> prodottiAssociati = List.copyOf(category.getProducts());
        for (Product p : prodottiAssociati) {
            p.getCategories().remove(category);
        }
        productRepository.saveAll(prodottiAssociati);

        // 2) Elimina la categoria
        categoryRepository.delete(category);
    }  // <- qui finisce deleteCategory

    @Transactional
    public Category updateCategory(Long id, Category updatedCategory) {
        return categoryRepository.findById(id).map(category -> {
            category.setName(
                    Objects.requireNonNullElse(updatedCategory.getName(), category.getName())
            );
            category.setDescription(
                    Objects.requireNonNullElse(updatedCategory.getDescription(), category.getDescription())
            );
            return categoryRepository.save(category);
        }).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Transactional
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
}
