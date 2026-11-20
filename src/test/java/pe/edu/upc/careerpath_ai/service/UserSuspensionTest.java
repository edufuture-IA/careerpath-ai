package pe.edu.upc.careerpath_ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.upc.careerpath_ai.model.Role;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.repository.*;
import pe.edu.upc.careerpath_ai.service.impl.UserServiceImpl;
import pe.edu.upc.careerpath_ai.service.security.UserDetailsServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserSuspensionTest {

    private final UserRepository users = mock(UserRepository.class);

    private User student(boolean enabled) {
        return User.builder().id(7L).username("alumno@upc.edu.pe").password("hash")
                .fullName("Alumno").role(Role.ESTUDIANTE).enabled(enabled).build();
    }

    @Test
    void nuevoUsuarioQuedaActivoPorDefecto() {
        User u = User.builder().username("a@b.com").build();
        assertTrue(u.isEnabled());
    }

    @Test
    void cuentaSuspendidaApareceDeshabilitadaEnSpringSecurity() {
        when(users.findByUsername("alumno@upc.edu.pe")).thenReturn(Optional.of(student(false)));
        UserDetails details = new UserDetailsServiceImpl(users).loadUserByUsername("alumno@upc.edu.pe");
        assertFalse(details.isEnabled());
    }

    @Test
    void cuentaActivaApareceHabilitada() {
        when(users.findByUsername("alumno@upc.edu.pe")).thenReturn(Optional.of(student(true)));
        UserDetails details = new UserDetailsServiceImpl(users).loadUserByUsername("alumno@upc.edu.pe");
        assertTrue(details.isEnabled());
    }

    @Test
    void setEnabledSuspendeYReactivaYGuarda() {
        User u = student(true);
        when(users.findById(7L)).thenReturn(Optional.of(u));
        UserServiceImpl service = new UserServiceImpl(users, mock(InstitutionRepository.class),
                null, mock(ClassroomRepository.class), mock(TestResultRepository.class));

        service.setEnabled(7L, false);
        assertFalse(u.isEnabled());

        service.setEnabled(7L, true);
        assertTrue(u.isEnabled());
        verify(users, times(2)).save(u);
    }
}
