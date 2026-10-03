package pe.edu.upc.careerpath_ai.dto;

import pe.edu.upc.careerpath_ai.model.Role;
import pe.edu.upc.careerpath_ai.model.User;

/** Vista pública del usuario: nunca expone el password. */
public record UserDTO(Long id, String username, String fullName, Role role) {
    public static UserDTO from(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getFullName(), user.getRole());
    }
}
