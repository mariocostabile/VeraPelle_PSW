// src/main/java/psw/verapelle/repository/ProductRepository.java
package psw.verapelle.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import psw.verapelle.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Pagina tutti i prodotti che appartengono alla categoria specificata.
     */
    Page<Product> findAllByCategories_Id(Long categoryId, Pageable pageable);

}
