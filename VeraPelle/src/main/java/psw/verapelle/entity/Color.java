// src/main/java/psw/verapelle/entity/Color.java
package psw.verapelle.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "colors")
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome del colore (es. "Rosso") */
    @Column(nullable = false)
    private String name;

    /** Codice esadecimale del colore (es. "#FF0000") */
    @Column(name = "hex_code", nullable = false, length = 7)
    private String hexCode;

    @Version
    @Column(name = "version")
    private Integer version;
}
