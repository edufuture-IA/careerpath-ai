package pe.edu.upc.careerpath_ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pe.edu.upc.careerpath_ai.model.Role;

@Data
public class RegisterForm {

    @NotBlank
    private String fullName;

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 8, message = "Mínimo 8 caracteres")
    private String password;

    private Role role;

    // 🆕 Código de invitación (opcional)
    private String invitationCode;
}