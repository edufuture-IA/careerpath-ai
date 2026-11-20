package pe.edu.upc.careerpath_ai.service;

import org.junit.jupiter.api.Test;
import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.model.University;
import pe.edu.upc.careerpath_ai.repository.TestResultRepository;
import pe.edu.upc.careerpath_ai.repository.UniversityRepository;
import pe.edu.upc.careerpath_ai.service.impl.UniversityServiceImpl;
import pe.edu.upc.careerpath_ai.util.AffinityStats;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// US20 y US21 a nivel de servicio
class UniversityOwnInfoTest {

    private final UniversityRepository universities = mock(UniversityRepository.class);
    private final TestResultRepository results = mock(TestResultRepository.class);
    private final UniversityServiceImpl service = new UniversityServiceImpl(universities, results);

    @Test
    void editarDatosPropiosNoCambiaElNombre() {
        University uni = University.builder().id(1L).name("UPC").city("Lima").build();
        when(universities.findById(1L)).thenReturn(Optional.of(uni));
        when(universities.save(any(University.class))).thenAnswer(i -> i.getArgument(0));

        University saved = service.updateOwnInfo(1L, "https://upc.edu.pe", "Surco",
                "", "Nueva descripcion", "Ingeniería,Salud");

        assertEquals("UPC", saved.getName());
        assertEquals("Surco", saved.getCity());
        assertEquals("Nueva descripcion", saved.getDescription());
        assertEquals("Ingeniería,Salud", saved.getFaculties());
    }

    @Test
    void estadisticasSoloConsideranLasCarrerasDeLaUniversidad() {
        University uni = University.builder().id(1L).name("UPC").build();
        uni.getCareers().add(Career.builder().name("Derecho").build());
        TestResult r1 = new TestResult();
        r1.setRecommendedCareers("Derecho, Medicina Humana");
        TestResult r2 = new TestResult();
        r2.setRecommendedCareers("Medicina Humana");
        when(results.findAll()).thenReturn(List.of(r1, r2));

        AffinityStats.Result stats = service.affinityStats(uni);

        assertEquals(2, stats.totalEvaluations());
        assertEquals(1, stats.rows().size());
        assertEquals(new AffinityStats.Row("Derecho", 1, 50.0), stats.rows().get(0));
    }
}
