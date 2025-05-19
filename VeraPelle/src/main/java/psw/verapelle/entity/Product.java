// src/main/java/psw/verapelle/entity/Product.java
package psw.verapelle.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    /**
     * Relazione con le categorie.
     * Non serializziamo direttamente la lista per evitare ricorsioni o payload troppo grandi.
     */
    @ManyToMany
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @JsonIgnore
    private List<Category> categories = new ArrayList<>();

    /**
     * Relazione con i colori del prodotto.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_colors",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "color_id")
    )
    @JsonIgnore
    private List<Color> colors = new ArrayList<>();

    @Version
    @Column(name = "version")
    private Integer version;

    /**
     * Relazione con le immagini del prodotto.
     * JPA caricherà la lista di ProductImage collegate via product_id.
     */
    @OneToMany(
            mappedBy = "product",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<ProductImage> images = new ArrayList<>();
}
