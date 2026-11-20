package pe.edu.upc.careerpath_ai.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.service.AIService;
import pe.edu.upc.careerpath_ai.service.ExportService;
import pe.edu.upc.careerpath_ai.service.TestService;
import pe.edu.upc.careerpath_ai.service.UserService;

import java.util.Optional;

import static org.mockito.Mockito.*;

// US10: el estudiante solo puede descargar el PDF de sus propios resultados
class StudentPdfTest {

    private final UserService userService = mock(UserService.class);
    private final TestService testService = mock(TestService.class);
    private final ExportService exportService = mock(ExportService.class);
    private final StudentController controller =
            new StudentController(userService, testService, mock(AIService.class), exportService);

    private TestResult resultOf(long studentId) {
        User owner = User.builder().id(studentId).build();
        TestResult r = new TestResult();
        r.setStudent(owner);
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
    void duenoDescargaSuPdf() throws Exception {
        TestResult r = resultOf(1L);
        when(testService.findById(10L)).thenReturn(r);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        controller.downloadPdf(10L, loginAs(1L), resp);

        verify(exportService).exportTestResultToPdf(r, resp);
    }

    @Test
    void otroEstudianteNoPuedeDescargarElPdf() throws Exception {
        when(testService.findById(10L)).thenReturn(resultOf(2L));
        HttpServletResponse resp = mock(HttpServletResponse.class);

        controller.downloadPdf(10L, loginAs(1L), resp);

        verify(exportService, never()).exportTestResultToPdf(any(), any());
        verify(resp).sendRedirect("/student/dashboard");
    }
}
