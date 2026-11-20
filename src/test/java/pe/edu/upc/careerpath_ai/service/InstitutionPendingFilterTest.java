package pe.edu.upc.careerpath_ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upc.careerpath_ai.model.Institution;
import pe.edu.upc.careerpath_ai.model.Role;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.repository.*;
import pe.edu.upc.careerpath_ai.service.impl.InstitutionServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// US16: el colegio filtra a los alumnos que aun no completan el test
class InstitutionPendingFilterTest {

    private final UserRepository users = mock(UserRepository.class);
    private final TestResultRepository results = mock(TestResultRepository.class);
    private final InstitutionServiceImpl service = new InstitutionServiceImpl(
            mock(InstitutionRepository.class), mock(ClassroomRepository.class),
            users, results, mock(PasswordEncoder.class));

    private User student(long id, String name) {
        return User.builder().id(id).fullName(name).username(name + "@cole.pe")
                .role(Role.ESTUDIANTE).institution(Institution.builder().id(1L).build()).build();
    }

    @Test
    void conElFiltroSoloQuedanLosAlumnosSinTest() {
        User ana = student(1, "ana");
        User beto = student(2, "beto");
        when(users.findByRole(Role.ESTUDIANTE)).thenReturn(List.of(ana, beto));
        when(results.findByStudentOrderByCompletedAtDesc(ana)).thenReturn(List.of(new TestResult()));
        when(results.findByStudentOrderByCompletedAtDesc(beto)).thenReturn(List.of());

        List<User> pendientes = service.filterStudents(1L, null, null, true);

        assertEquals(List.of(beto), pendientes);
    }

    @Test
    void sinElFiltroSalenTodos() {
        User ana = student(1, "ana");
        User beto = student(2, "beto");
        when(users.findByRole(Role.ESTUDIANTE)).thenReturn(List.of(ana, beto));

        List<User> todos = service.filterStudents(1L, null, null, false);

        assertEquals(2, todos.size());
        verify(results, never()).findByStudentOrderByCompletedAtDesc(any());
    }
}
