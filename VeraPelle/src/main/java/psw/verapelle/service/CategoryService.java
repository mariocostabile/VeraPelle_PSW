package psw.verapelle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import psw.verapelle.DTO.CategoryDTO;
import psw.verapelle.entity.Category;
import psw.verapelle.repository.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Category createCategory(CategoryDTO categoryDTO) {
        Category category = new Category(null, categoryDTO.getName(), categoryDTO.getDescription(), null);
        return categoryRepository.save(category);
    }

}
