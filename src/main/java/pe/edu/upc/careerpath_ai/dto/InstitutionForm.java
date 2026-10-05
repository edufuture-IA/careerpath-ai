package pe.edu.upc.careerpath_ai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InstitutionForm {

    @NotBlank private String name;
    @NotBlank @Email private String contactEmail;
    private String address;
    private String phone;

    @NotBlank private String adminFullName;
    @NotBlank @Size(min = 8, message = "Mínimo 8 caracteres") private String adminPassword;
}