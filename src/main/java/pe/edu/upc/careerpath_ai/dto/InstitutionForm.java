package pe.edu.upc.careerpath_ai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InstitutionForm {

    @NotBlank private String name;
    @NotBlank @Email private String contactEmail;
    private String address;
    private String phone;

    @NotBlank private String adminFullName;
    @NotBlank private String adminPassword;
}