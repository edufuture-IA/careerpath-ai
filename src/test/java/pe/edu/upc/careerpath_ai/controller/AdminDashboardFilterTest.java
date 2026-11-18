package pe.edu.upc.careerpath_ai.controller;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import pe.edu.upc.careerpath_ai.model.Role;
import pe.edu.upc.careerpath_ai.repository.*;
import pe.edu.upc.careerpath_ai.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AdminDashboardFilterTest {

    private final UserRepository users = mock(UserRepository.class);
    private final TestResultRepository results = mock(TestResultRepository.class);
    private final UserService userService = mock(UserService.class);
    private final AdminController controller = new AdminController(
            userService, mock(QuestionService.class), mock(InstitutionService.class), mock(TestService.class),
            mock(ActivityLogService.class), results, mock(CareerRepository.class), mock(ContactMessageRepository.class),
            mock(NewsletterSubscriberRepository.class), users, null);

    @Test
    void conRangoUsaConteosPorFecha() {
        when(users.countByRoleAndCreatedAtBetween(eq(Role.ESTUDIANTE), any(), any())).thenReturn(4L);
        when(results.countByCompletedAtBetween(any(), any())).thenReturn(7L);
        var model = new ExtendedModelMap();

        controller.dashboard(model, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        assertEquals(4L, model.get("totalStudents"));
        assertEquals(7L, model.get("totalTests"));
        assertTrue((Boolean) model.get("filtered"));
        verify(results).countByCompletedAtBetween(
                LocalDate.of(2026, 10, 1).atStartOfDay(), LocalDate.of(2026, 10, 31).atTime(23, 59, 59, 999_999_999));
    }

    @Test
    void sinFechasUsaTotalesCompletos() {
        when(results.count()).thenReturn(9L);
        var model = new ExtendedModelMap();

        controller.dashboard(model, null, null);

        assertEquals(9L, model.get("totalTests"));
        verify(results, never()).countByCompletedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void rangoInvertidoNoFiltra() {
        var model = new ExtendedModelMap();

        controller.dashboard(model, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 10, 1));

        assertTrue((Boolean) model.get("invalidRange"));
        assertEquals(false, model.get("filtered"));
    }
}
