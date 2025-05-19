package psw.verapelle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import psw.verapelle.DTO.CategoryDTO;
import psw.verapelle.entity.Category;
import psw.verapelle.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")  // Modificato da "/api/admin/categories"
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /** LETTURA PUBBLICA DI TUTTE LE CATEGORIE */
    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        return categoryService.getAllCategories().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** DETTAGLIO CATEGORIA (solo ADMIN) */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{id}")
    public CategoryDTO getCategoryById(@PathVariable Long id) {
        return toDTO(categoryService.getCategoryById(id));
    }

    /** CREAZIONE CATEGORIA (solo ADMIN) */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/create")
    public CategoryDTO createCategory(@RequestBody CategoryDTO categoryDTO) {
        Category created = categoryService.createCategory(categoryDTO);
        return toDTO(created);
    }

    /** MODIFICA CATEGORIA (solo ADMIN) */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}")
    public CategoryDTO updateCategory(@PathVariable Long id,
                                      @RequestBody CategoryDTO categoryDTO) {
        Category toUpdate = new Category();
        toUpdate.setName(categoryDTO.getName());
        toUpdate.setDescription(categoryDTO.getDescription());
        Category updated = categoryService.updateCategory(id, toUpdate);
        return toDTO(updated);
    }

    /** CANCELLAZIONE CATEGORIA (solo ADMIN) */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("admin/{id}")
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }

    /** MAPPER ENTITY → DTO */
    private CategoryDTO toDTO(Category c) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());
        return dto;
    }
}
