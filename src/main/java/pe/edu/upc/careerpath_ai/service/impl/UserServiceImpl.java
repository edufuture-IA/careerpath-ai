package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.edu.upc.careerpath_ai.dto.RegisterForm;
import pe.edu.upc.careerpath_ai.exception.ConflictoException;
import pe.edu.upc.careerpath_ai.exception.RecursoNoEncontradoException;
import pe.edu.upc.careerpath_ai.model.Institution;
import pe.edu.upc.careerpath_ai.model.Role;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.repository.InstitutionRepository;
import pe.edu.upc.careerpath_ai.repository.UserRepository;
import pe.edu.upc.careerpath_ai.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;  // 🆕
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterForm form) {
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new ConflictoException("El correo ya está registrado");
        }

        User user = User.builder()
                .username(form.getUsername())
                .password(passwordEncoder.encode(form.getPassword()))
                .fullName(form.getFullName())
                .role(form.getRole())
                .build();

        // 🆕 Vincular a institución si viene código de invitación
        if (form.getInvitationCode() != null && !form.getInvitationCode().isBlank()) {
            Institution inst = institutionRepository
                    .findByInvitationCode(form.getInvitationCode().trim().toUpperCase())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Código de invitación inválido: " + form.getInvitationCode()));
            user.setInstitution(inst);
        }

        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    @Override
    public void delete(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }
}