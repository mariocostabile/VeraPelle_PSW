package psw.verapelle.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // nome del file così come lo salviamo su disco
    @Column(nullable = false)
    private String filename;

    // path relativo da esporre al client, es. "/uploads/<filename>"
    @Column(nullable = false)
    private String urlPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
