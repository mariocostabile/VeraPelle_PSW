package psw.verapelle.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psw.verapelle.DTO.CategoryDTO;
import psw.verapelle.entity.Category;
import psw.verapelle.repository.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public Category createCategory(CategoryDTO categoryDTO) {
        Category category = new Category(null, categoryDTO.getName(), categoryDTO.getDescription(), null, null);
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        categoryRepository.delete(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category updatedCategory) {
        return categoryRepository.findById(id).map(category -> {
            category.setName(updatedCategory.getName());
            category.setDescription(updatedCategory.getDescription());
            return categoryRepository.save(category);
        }).orElseThrow(() -> new RuntimeException("Category not found"));
    }

}
