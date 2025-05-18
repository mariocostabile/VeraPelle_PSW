// src/main/java/psw/verapelle/controller/ProductImageController.java
package psw.verapelle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import psw.verapelle.entity.ProductImage;
import psw.verapelle.service.ProductImageService;
// IMPORT CORRETTO
import psw.verapelle.dto.ProductImageDTO;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/products/{productId}/images")
public class ProductImageController {

    private final ProductImageService imageService;

    public ProductImageController(ProductImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductImageDTO>> uploadImages(
            @PathVariable Long productId,
            @RequestParam("files") MultipartFile[] files
    ) throws IOException {
        List<ProductImage> saved = imageService.saveImages(productId, files);
        List<ProductImageDTO> dto = saved.stream()
                .map(pi -> new ProductImageDTO(pi.getId(), pi.getUrlPath()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<ProductImageDTO>> getImages(@PathVariable Long productId) {
        List<ProductImage> imgs = imageService.getImagesByProduct(productId);
        List<ProductImageDTO> dto = imgs.stream()
                .map(pi -> new ProductImageDTO(pi.getId(), pi.getUrlPath()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {
        imageService.deleteImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }
}
