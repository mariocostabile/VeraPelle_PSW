package psw.verapelle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import psw.verapelle.entity.ProductVariant;

import java.util.Optional;

/**
 * Repository per la gestione delle varianti colore di ogni prodotto.
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /**
     * Recupera la variante specifica per prodotto e colore.
     */
    Optional<ProductVariant> findByProductIdAndColorId(Long productId, Long colorId);

    /**
     * Somma lo stock di tutte le varianti di un prodotto (opzionale).
     */
    @Query("SELECT SUM(v.stockQuantity) FROM ProductVariant v WHERE v.product.id = :prodId")
    Integer sumStockByProduct(@Param("prodId") Long productId);
}