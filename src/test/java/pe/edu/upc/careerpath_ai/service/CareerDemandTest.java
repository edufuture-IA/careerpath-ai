package pe.edu.upc.careerpath_ai.service;

import org.junit.jupiter.api.Test;
import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.repository.CareerRepository;
import pe.edu.upc.careerpath_ai.service.impl.CareerServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CareerDemandTest {

    private final CareerRepository repository = mock(CareerRepository.class);
    private final AIService ai = mock(AIService.class);
    private final CareerServiceImpl service = new CareerServiceImpl(repository, ai);

    private Career stubSave(Career career) {
        when(repository.save(any())).thenAnswer(i -> {
            Career c = i.getArgument(0);
            if (c.getId() == null) c.setId(1L);
            return c;
        });
        when(repository.findById(1L)).thenReturn(Optional.of(career));
        return career;
    }

    @Test
    void carreraNuevaIgnoraLaDemandaEnviadaYUsaLaIA() {
        Career career = stubSave(Career.builder().name("Derecho").demandIndex(0.99).build());
        when(ai.estimateDemandIndex(career)).thenReturn(0.64);

        Career saved = service.save(career);

        assertEquals(0.64, saved.getDemandIndex());
        assertEquals("AI_GEMINI", saved.getDemandIndexSource());
        assertNotNull(saved.getDemandIndexUpdatedAt());
    }

    @Test
    void siLaIAFallaQuedaPendienteParaReintentar() {
        Career career = stubSave(Career.builder().name("Derecho").demandIndex(0.99).build());
        when(ai.estimateDemandIndex(career)).thenReturn(null);

        Career saved = service.save(career);

        assertEquals(0.0, saved.getDemandIndex());
        assertEquals("PENDING", saved.getDemandIndexSource());
        assertNull(saved.getDemandIndexUpdatedAt());
    }

    @Test
    void guardarCarreraExistenteNoRecalculaLaDemanda() {
        Career existing = Career.builder().id(5L).name("Derecho").demandIndex(0.7).build();
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.save(existing);

        verify(ai, never()).estimateDemandIndex(any());
        assertEquals(0.7, existing.getDemandIndex());
    }

    @Test
    void editarNoCambiaLaDemandaGuardada() {
        Career existing = Career.builder().id(5L).name("Derecho").demandIndex(0.7).build();
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.update(5L, Career.builder().name("Derecho Penal").demandIndex(0.0).build());

        assertEquals("Derecho Penal", existing.getName());
        assertEquals(0.7, existing.getDemandIndex());
    }
}
