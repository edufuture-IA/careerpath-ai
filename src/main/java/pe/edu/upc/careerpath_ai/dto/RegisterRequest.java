package pe.edu.upc.careerpath_ai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.upc.careerpath_ai.model.Role;

public record RegisterRequest(
        @NotBlank(message = "El nombre completo es obligatorio") String fullName,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo inválido") String username,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "Mínimo 8 caracteres") String password,
        @NotNull(message = "El rol es obligatorio") Role role,
        String invitationCode   // 🆕 Opcional
) {}