package pe.edu.upc.careerpath_ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upc.careerpath_ai.model.Role;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.repository.*;
import pe.edu.upc.careerpath_ai.service.impl.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceDeleteAccountTest {

    private final UserRepository users = mock(UserRepository.class);
    private final ClassroomRepository classrooms = mock(ClassroomRepository.class);
    private final TestResultRepository results = mock(TestResultRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final UserServiceImpl service =
            new UserServiceImpl(users, mock(InstitutionRepository.class), encoder, classrooms, results);

    private User user(Role role) {
        User u = User.builder().id(1L).username("a@b.com").password("hash").role(role).build();
        when(users.findByUsername("a@b.com")).thenReturn(Optional.of(u));
        when(classrooms.findByStudent(u)).thenReturn(Optional.empty());
        when(classrooms.findByTeacher(u)).thenReturn(Optional.empty());
        return u;
    }

    @Test
    void borraResultadosYUsuarioConPasswordCorrecta() {
        User u = user(Role.ESTUDIANTE);
        List<TestResult> rs = List.of(new TestResult());
        when(encoder.matches("ok", "hash")).thenReturn(true);
        when(results.findByStudentOrderByCompletedAtDesc(u)).thenReturn(rs);

        service.deleteAccount("a@b.com", "ok");

        verify(results).deleteAll(rs);
        verify(users).delete(u);
    }

    @Test
    void passwordIncorrectaNoBorraNada() {
        user(Role.ESTUDIANTE);
        when(encoder.matches(any(), any())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> service.deleteAccount("a@b.com", "mal"));
        verify(users, never()).delete(any());
    }

    @Test
    void soloEstudiantes() {
        user(Role.DOCENTE);
        assertThrows(AccessDeniedException.class, () -> service.deleteAccount("a@b.com", "x"));
        verify(users, never()).delete(any());
    }
}
