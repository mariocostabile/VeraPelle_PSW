package psw.verapelle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import psw.verapelle.entity.Color;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {

    /**
     * Rimuove tutte le associazioni nella join table product_colors
     * per il colore indicato da colorId.
     */
    @Modifying
    @Transactional
    @Query(
            value = "DELETE FROM product_colors WHERE color_id = :colorId",
            nativeQuery = true
    )
    void deleteProductAssociations(@Param("colorId") Long colorId);
}
