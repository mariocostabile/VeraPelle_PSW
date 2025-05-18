package psw.verapelle.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import psw.verapelle.entity.Product;
import psw.verapelle.entity.ProductImage;
import psw.verapelle.repository.ProductImageRepository;
import psw.verapelle.repository.ProductRepository;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductImageService {

    @Value("${app.uploads.dir:${user.home}/uploads}")
    private String uploadDir;

    private final ProductImageRepository imageRepo;
    private final ProductRepository productRepo;

    public ProductImageService(ProductImageRepository imageRepo,
                               ProductRepository productRepo) {
        this.imageRepo = imageRepo;
        this.productRepo = productRepo;
    }

    public List<ProductImage> saveImages(Long productId, MultipartFile[] files) throws IOException {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        Path uploadPath = Paths.get(uploadDir);
        if (Files.notExists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        List<ProductImage> saved = new ArrayList<>();
        for (MultipartFile file : files) {
            String original = file.getOriginalFilename();
            String filename = System.currentTimeMillis() + "_" + original;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            ProductImage pi = new ProductImage();
            pi.setFilename(filename);
            pi.setUrlPath("/uploads/" + filename);
            pi.setProduct(product);
            saved.add(imageRepo.save(pi));
        }
        return saved;
    }

    public List<ProductImage> getImagesByProduct(Long productId) {
        return imageRepo.findByProductId(productId);
    }

    public void deleteImage(Long productId, Long imageId) {
        ProductImage pi = imageRepo.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));

        if (!pi.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to product " + productId);
        }

        Path filePath = Paths.get(uploadDir).resolve(pi.getFilename());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + filePath, e);
        }

        imageRepo.delete(pi);
    }
}
