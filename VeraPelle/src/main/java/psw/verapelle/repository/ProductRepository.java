package psw.verapelle.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import psw.verapelle.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Pagina tutti i prodotti che appartengono alla categoria specificata.
     */
    Page<Product> findAllByCategories_Id(Long categoryId, Pageable pageable);

    /**
     * Pagina tutti i prodotti che appartengono a tutte le categorie specificate.
     */
    @Query("""
      SELECT p
        FROM Product p
        JOIN p.categories c
       WHERE c.id IN :categoryIds
       GROUP BY p
      HAVING COUNT(DISTINCT c.id) = :#{#categoryIds.size()}
      """)
    Page<Product> findByAllCategories(
            @Param("categoryIds") List<Long> categoryIds,
            Pageable pageable
    );

}
