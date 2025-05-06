// src/main/java/psw/verapelle/entity/Category.java

package psw.verapelle.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @ManyToMany(mappedBy = "categories")
    private List<Product> products = new ArrayList<>();

    @Version
    @Column(name = "version")
    private Integer version;
}
