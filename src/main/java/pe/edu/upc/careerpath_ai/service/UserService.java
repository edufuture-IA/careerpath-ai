package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.dto.RegisterForm;
import pe.edu.upc.careerpath_ai.model.Role;
import pe.edu.upc.careerpath_ai.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User register(RegisterForm form);
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);
    User findById(Long id);
    void delete(Long id);
    /** US37: borra la cuenta de un ESTUDIANTE y sus datos, previa validación de contraseña. */
    void deleteAccount(String username, String rawPassword);          
}