// src/main/java/psw/verapelle/repository/ColorRepository.java
package psw.verapelle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import psw.verapelle.entity.Color;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {
    // estensioni CRUD basi ereditate da JpaRepository
}
