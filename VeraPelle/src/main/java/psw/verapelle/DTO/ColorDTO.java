// src/main/java/psw/verapelle/DTO/ColorDTO.java
package psw.verapelle.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ColorDTO {
    private Long id;
    private String name;
    private String hexCode;
}
