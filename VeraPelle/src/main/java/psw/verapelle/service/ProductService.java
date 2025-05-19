package psw.verapelle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psw.verapelle.DTO.ProductDTO;
import psw.verapelle.entity.Category;
import psw.verapelle.entity.Product;
import psw.verapelle.repository.CategoryRepository;
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

    /**
     * Crea un nuovo prodotto (solo ADMIN).
     */
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

        List<Category> categories = productDTO.getCategoryIds().stream()
                .map(id -> categoryRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Category not found: " + id)))
                .collect(Collectors.toList());
        product.setCategories(categories);

        return productRepository.save(product);
    }

    /**
     * Aggiorna un prodotto esistente (solo ADMIN).
     */
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
                    if (productDTO.getStockQuantity() != 0) {
                        product.setStockQuantity(productDTO.getStockQuantity());
                    }
                    if (productDTO.getCategoryIds() != null) {
                        List<Category> cats = productDTO.getCategoryIds().stream()
                                .map(cid -> categoryRepository.findById(cid)
                                        .orElseThrow(() -> new RuntimeException("Category not found: " + cid)))
                                .collect(Collectors.toList());
                        product.setCategories(cats);
                    }
                    return productRepository.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    /**
     * Elimina un prodotto (solo ADMIN).
     */
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        productRepository.delete(product);
    }

    /**
     * Ritorna tutti i prodotti (solo ADMIN, senza paginazione).
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Ritorna un prodotto per ID (solo ADMIN).
     */
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    // ──────────── Metodi paginati / con filtro ────────────

    /**
     * Ritorna una pagina di prodotti, eventualmente filtrati per categoria.
     *
     * @param categoryId opzionale: se presente, restituisce solo i prodotti di quella categoria
     * @param pageable   parametri di paging (page, size, sort)
     */
    @Transactional(readOnly = true)
    public Page<Product> findAll(Optional<Long> categoryId, Pageable pageable) {
        if (categoryId.isPresent()) {
            return productRepository.findAllByCategories_Id(categoryId.get(), pageable);
        }
        return productRepository.findAll(pageable);
    }

    /**
     * Ritorna una pagina di prodotti, eventualmente filtrati per tutte le categorie specificate.
     *
     * @param categoryIds lista di ID categorie
     * @param pageable    parametri di paging (page, size, sort)
     */
    @Transactional(readOnly = true)
    public Page<Product> findAllByCategories(List<Long> categoryIds, Pageable pageable) {
        if (categoryIds != null && !categoryIds.isEmpty()) {
            return productRepository.findByAllCategories(categoryIds, pageable);
        }
        return productRepository.findAll(pageable);
    }
}
