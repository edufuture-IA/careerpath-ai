package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.careerpath_ai.dto.RegisterForm;
import pe.edu.upc.careerpath_ai.exception.ConflictoException;
import pe.edu.upc.careerpath_ai.exception.RecursoNoEncontradoException;
import pe.edu.upc.careerpath_ai.model.Institution;
import pe.edu.upc.careerpath_ai.model.Role;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.repository.ClassroomRepository;
import pe.edu.upc.careerpath_ai.repository.InstitutionRepository;
import pe.edu.upc.careerpath_ai.repository.TestResultRepository;
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
    private final ClassroomRepository classroomRepository;
    private final TestResultRepository testResultRepository;

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
    @Transactional
    public void delete(Long id) {
        purge(findById(id));
    }

    @Override
    @Transactional
    public void deleteAccount(String username, String rawPassword) {
        User user = findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (user.getRole() != Role.ESTUDIANTE) {
            throw new AccessDeniedException("Solo los estudiantes pueden eliminar su cuenta");
        }
        if (rawPassword == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }
        purge(user);
    }

    // Quita al usuario de sus aulas (tablas de unión) y borra sus resultados antes que la fila,
    // si no la FK de test_results / classroom_* bloquea el borrado. Favoritos caen por cascada.
    private void purge(User user) {
        classroomRepository.findByStudent(user).ifPresent(c -> {
            c.getStudents().remove(user);
            classroomRepository.save(c);
        });
        classroomRepository.findByTeacher(user).ifPresent(c -> {
            c.getTeachers().remove(user);
            classroomRepository.save(c);
        });
        testResultRepository.deleteAll(testResultRepository.findByStudentOrderByCompletedAtDesc(user));
        userRepository.delete(user);
    }
}