package pe.edu.upc.careerpath_ai.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/{id}")
    public UserDTO get(@PathVariable Long id) {
        return UserDTO.from(userService.findById(id));
    }
}
