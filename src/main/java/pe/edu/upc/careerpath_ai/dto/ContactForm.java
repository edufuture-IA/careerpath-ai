package pe.edu.upc.careerpath_ai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactForm {

    @NotBlank private String fullName;
    @NotBlank @Email private String email;
    private String institutionName;
    private String phone;

    @NotBlank @Size(min = 10, max = 2000)
    private String message;
}