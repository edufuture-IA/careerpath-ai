package pe.edu.upc.careerpath_ai.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.dto.DeleteAccountRequest;
import pe.edu.upc.careerpath_ai.dto.UserDTO;
import pe.edu.upc.careerpath_ai.model.Role;
import pe.edu.upc.careerpath_ai.service.UserService;

import java.util.List;

/** Consulta de usuarios: solo DOCENTE y ADMIN (ver SecurityConfig). Nunca expone passwords. */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;

    @GetMapping("/students")
    public List<UserDTO> students() {
        return userService.findByRole(Role.ESTUDIANTE).stream().map(UserDTO::from).toList();
    }

    /** US37: el estudiante borra su propia cuenta confirmando su contraseña. */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(@Valid @RequestBody DeleteAccountRequest req, Authentication auth) {
        userService.deleteAccount(auth.getName(), req.password());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public UserDTO get(@PathVariable Long id) {
        return UserDTO.from(userService.findById(id));
    }
}
