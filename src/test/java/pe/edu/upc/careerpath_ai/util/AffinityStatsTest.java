package pe.edu.upc.careerpath_ai.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// US21: estadisticas agrupadas de afinidad por carrera
class AffinityStatsTest {

    @Test
    void cuentaCuantasEvaluacionesRecomiendanCadaCarrera() {
        List<String> recomendadas = List.of(
                "Ingeniería de Sistemas, Medicina Humana",
                "ingeniería de sistemas,Derecho",
                "Derecho");

        AffinityStats.Result r = AffinityStats.compute(
                List.of("Ingeniería de Sistemas", "Derecho", "Arquitectura"), recomendadas);

        assertEquals(3, r.totalEvaluations());
        assertEquals(new AffinityStats.Row("Derecho", 2, 66.7), r.rows().get(0));
        assertEquals(new AffinityStats.Row("Ingeniería de Sistemas", 2, 66.7), r.rows().get(1));
        assertEquals(new AffinityStats.Row("Arquitectura", 0, 0.0), r.rows().get(2));
    }

    @Test
    void sinEvaluacionesTodoQuedaEnCero() {
        AffinityStats.Result r = AffinityStats.compute(List.of("Derecho"), List.of());

        assertEquals(0, r.totalEvaluations());
        assertEquals(new AffinityStats.Row("Derecho", 0, 0.0), r.rows().get(0));
    }

    @Test
    void ignoraEvaluacionesSinRecomendaciones() {
        AffinityStats.Result r = AffinityStats.compute(
                List.of("Derecho"), java.util.Arrays.asList(null, "", "Derecho"));

        assertEquals(3, r.totalEvaluations());
        assertEquals(1, r.rows().get(0).evaluations());
    }
}
