package psw.verapelle.DTO;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO {
    private String id;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String dateOfBirth;
    private String address;
    private String phone;
    @Email
    @NotBlank
    private String email;
}
