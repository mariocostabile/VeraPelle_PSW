package psw.verapelle.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psw.verapelle.DTO.ProductDTO;
import psw.verapelle.DTO.VariantDTO;
import psw.verapelle.entity.Color;
import psw.verapelle.entity.Product;
import psw.verapelle.entity.ProductVariant;
import psw.verapelle.repository.CategoryRepository;
import psw.verapelle.repository.ColorRepository;
import psw.verapelle.repository.ProductRepository;
import psw.verapelle.repository.ProductVariantRepository;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Transactional
    public Product createProduct(ProductDTO productDTO) {
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        // stockQuantity è deprecato; ora gestito tramite varianti colore

        // categorie
        product.setCategories(
                productDTO.getCategoryIds().stream()
                        .map(id -> categoryRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Category not found: " + id)))
                        .collect(Collectors.toList())
        );

        // colori disponibili: li prendiamo dalle varianti
        if (productDTO.getVariants() != null) {
            product.setColors(
            productDTO.getVariants().stream()
            .map(vdto -> colorRepository.findById(vdto.getColorId())
            .orElseThrow(() -> new RuntimeException("Color not found: " + vdto.getColorId())))
            .distinct()
            .collect(Collectors.toList())
            );
        }

        // 1️⃣ salvo il prodotto base per ottenere l’ID
        Product saved = productRepository.save(product);

        // 2️⃣ creo le varianti colore con stock dedicato
        for (VariantDTO vdto : productDTO.getVariants()) {
            Color c = colorRepository.findById(vdto.getColorId())
                    .orElseThrow(() -> new RuntimeException("Color not found: " + vdto.getColorId()));
            ProductVariant pv = new ProductVariant();
            pv.setProduct(saved);
            pv.setColor(c);
            pv.setStockQuantity(vdto.getStockQuantity());
            variantRepository.save(pv);
        }
        return saved;    // ***
    }

    @Transactional
    public Product updateProduct(Long id, ProductDTO productDTO) {
        return productRepository.findById(id)
                .map(p -> {
                    if (productDTO.getName() != null) {
                        p.setName(productDTO.getName());
                    }
                    if (productDTO.getDescription() != null) {
                        p.setDescription(productDTO.getDescription());
                    }
                    if (productDTO.getPrice() != null) {
                        p.setPrice(productDTO.getPrice());
                    }
                    // StockQuantity è opzionale: aggiorniamo solo se presente
                    if (productDTO.getStockQuantity() != null &&
                            productDTO.getStockQuantity() >= 0) {
                        p.setStockQuantity(productDTO.getStockQuantity());
                    }

                    if (productDTO.getCategoryIds() != null) {
                        p.setCategories(
                                productDTO.getCategoryIds().stream()
                                        .map(cid -> categoryRepository.findById(cid)
                                                .orElseThrow(() -> new RuntimeException("Category not found: " + cid)))
                                        .collect(Collectors.toList())
                        );
                    }

                    if (productDTO.getVariants() != null) {
                        p.setColors(
                                productDTO.getVariants().stream()
                                        .map(vdto -> colorRepository.findById(vdto.getColorId())
                                                .orElseThrow(() -> new RuntimeException("Color not found: " + vdto.getColorId())))
                                        .distinct()
                                        .collect(Collectors.toList())
                        );
                    }

                    // 3️⃣ sincronizzo le varianti colore
                    Map<Long, ProductVariant> existing = p.getVariants().stream()
                            .collect(Collectors.toMap(v -> v.getColor().getId(), v -> v));

                    for (VariantDTO vdto : productDTO.getVariants()) {
                        ProductVariant pv = existing.remove(vdto.getColorId());
                        if (pv != null) {
                            pv.setStockQuantity(vdto.getStockQuantity());
                        } else {
                            Color c = colorRepository.findById(vdto.getColorId())
                                    .orElseThrow(() -> new RuntimeException("Color not found: " + vdto.getColorId()));
                            pv = new ProductVariant();
                            pv.setProduct(p);
                            pv.setColor(c);
                            pv.setStockQuantity(vdto.getStockQuantity());
                            p.getVariants().add(pv);
                        }
                    }

                    // elimino le varianti rimosse dal form
                    for (ProductVariant toDelete : existing.values()) {
                        variantRepository.delete(toDelete);
                    }

                    // salvo e ritorno il prodotto aggiornato
                    return productRepository.save(p);
                })
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        productRepository.delete(product);
    }

    @Caching
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Caching
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

    /**
     * Suggestion: cerca prodotti il cui nome contiene (ignore case) la keyword.
     */
    @Transactional(readOnly = true)
    public List<Product> findByNameContainingIgnoreCase(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }
}
