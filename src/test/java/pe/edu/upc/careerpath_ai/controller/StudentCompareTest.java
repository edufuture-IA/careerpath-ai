package pe.edu.upc.careerpath_ai.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.service.AIService;
import pe.edu.upc.careerpath_ai.service.ExportService;
import pe.edu.upc.careerpath_ai.service.TestService;
import pe.edu.upc.careerpath_ai.service.UserService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// US11: el estudiante solo compara sus propias evaluaciones
class StudentCompareTest {

    private final UserService userService = mock(UserService.class);
    private final TestService testService = mock(TestService.class);
    private final StudentController controller = new StudentController(
            userService, testService, mock(AIService.class), mock(ExportService.class));

    private TestResult result(long studentId, LocalDateTime date, String scores) {
        TestResult r = new TestResult();
        r.setStudent(User.builder().id(studentId).build());
        r.setCompletedAt(date);
        r.setAreaScores(scores);
        return r;
    }

    private Authentication loginAs(long userId) {
        User me = User.builder().id(userId).username("alumno@upc.edu.pe").build();
        when(userService.findByUsername("alumno@upc.edu.pe")).thenReturn(Optional.of(me));
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("alumno@upc.edu.pe");
        return auth;
    }

    @Test
    void comparaDosEvaluacionesPropiasEnOrdenCronologico() {
        TestResult vieja = result(1L, LocalDateTime.of(2026, 10, 1, 10, 0), "Ingenieria:6");
        TestResult nueva = result(1L, LocalDateTime.of(2026, 11, 1, 10, 0), "Ingenieria:9");
        when(testService.findById(1L)).thenReturn(nueva);
        when(testService.findById(2L)).thenReturn(vieja);
        when(testService.parseAreaScores("Ingenieria:6")).thenReturn(Map.of("Ingenieria", 6));
        when(testService.parseAreaScores("Ingenieria:9")).thenReturn(Map.of("Ingenieria", 9));
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.compare(1L, 2L, model, loginAs(1L));

        assertEquals("student/compare", view);
        assertSame(vieja, model.get("before"));
        assertSame(nueva, model.get("after"));
    }

    @Test
    void noPermiteCompararEvaluacionesDeOtroEstudiante() {
        when(testService.findById(1L)).thenReturn(result(1L, LocalDateTime.now(), "Arte:3"));
        when(testService.findById(2L)).thenReturn(result(2L, LocalDateTime.now(), "Arte:5"));

        String view = controller.compare(1L, 2L, new ExtendedModelMap(), loginAs(1L));

        assertEquals("redirect:/student/dashboard", view);
    }

    @Test
    void noPermiteCompararUnaEvaluacionConsigoMisma() {
        String view = controller.compare(5L, 5L, new ExtendedModelMap(), loginAs(1L));

        assertEquals("redirect:/student/dashboard?compareError", view);
    }
}
