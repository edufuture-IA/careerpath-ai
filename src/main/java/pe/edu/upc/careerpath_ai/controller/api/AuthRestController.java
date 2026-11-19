package pe.edu.upc.careerpath_ai.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.dto.*;
import pe.edu.upc.careerpath_ai.exception.RecursoNoEncontradoException;
import pe.edu.upc.careerpath_ai.model.Role;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.security.JwtService;
import pe.edu.upc.careerpath_ai.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    /** Registro público. Solo ESTUDIANTE o DOCENTE; ADMIN no se puede auto-asignar. */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new AccessDeniedException("No se permite registrarse con el rol ADMIN");
        }
        RegisterForm form = new RegisterForm();
        form.setFullName(request.fullName());
        form.setUsername(request.username());
        form.setPassword(request.password());
        form.setRole(request.role());
        form.setInvitationCode(request.invitationCode());  // 🆕
        User created = userService.register(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDTO.from(created));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        UserDetails details = (UserDetails) auth.getPrincipal();
        User user = userService.findByUsername(details.getUsername())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return AuthResponse.bearer(jwtService.generateToken(details),
                jwtService.getExpirationMs(), UserDTO.from(user));
    }

    /** Perfil del usuario autenticado (requiere token). */
    @GetMapping("/me")
    public UserDTO me(Authentication auth) {
        return userService.findByUsername(auth.getName())
                .map(UserDTO::from)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
