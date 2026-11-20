package pe.edu.upc.careerpath_ai.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import pe.edu.upc.careerpath_ai.model.Role;
import pe.edu.upc.careerpath_ai.model.University;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.service.UniversityService;
import pe.edu.upc.careerpath_ai.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// US20: la universidad solo edita su propia institucion
class UniversityPanelControllerTest {

    private final UserService userService = mock(UserService.class);
    private final UniversityService universityService = mock(UniversityService.class);
    private final UniversityPanelController controller =
            new UniversityPanelController(userService, universityService);

    private Authentication loginWith(University uni) {
        User me = User.builder().id(9L).username("universidad@upc.edu.pe")
                .role(Role.UNIVERSIDAD).university(uni).build();
        when(userService.findByUsername("universidad@upc.edu.pe")).thenReturn(Optional.of(me));
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("universidad@upc.edu.pe");
        return auth;
    }

    @Test
    void guardaSiempreSobreLaUniversidadDelUsuarioLogueado() {
        Authentication auth = loginWith(University.builder().id(5L).name("UPC").build());

        String view = controller.save("https://upc.edu.pe", "Lima", "", "desc", "Salud", auth);

        assertEquals("redirect:/university/panel?saved", view);
        verify(universityService).updateOwnInfo(5L, "https://upc.edu.pe", "Lima", "", "desc", "Salud");
    }

    @Test
    void sinUniversidadAsociadaNoEditaNada() {
        Authentication auth = loginWith(null);

        String view = controller.save("", "", "", "", "", auth);

        assertEquals("redirect:/university/panel", view);
        verify(universityService, never())
                .updateOwnInfo(anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    void estadisticasSeCalculanParaLaUniversidadPropia() {
        University uni = University.builder().id(5L).name("UPC").build();
        Authentication auth = loginWith(uni);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.stats(auth, model);

        assertEquals("university/stats", view);
        assertSame(uni, model.get("university"));
        verify(universityService).affinityStats(eq(uni));
    }
}
