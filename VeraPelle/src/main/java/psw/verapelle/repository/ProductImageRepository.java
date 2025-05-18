package psw.verapelle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import psw.verapelle.entity.ProductImage;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);
}
