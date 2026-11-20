package pe.edu.upc.careerpath_ai.util;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

// US11: comparacion de puntajes por area entre dos evaluaciones
class ScoreComparisonTest {

    @Test
    void calculaElCambioPorArea() {
        Map<String, Integer> antes = new LinkedHashMap<>(Map.of("Ingenieria", 6));
        antes.put("Arte", 8);
        Map<String, Integer> despues = new LinkedHashMap<>(Map.of("Ingenieria", 10));
        despues.put("Arte", 5);

        List<ScoreComparison.Row> rows = ScoreComparison.compare(antes, despues);

        assertEquals(new ScoreComparison.Row("Ingenieria", 6, 10, 4), rows.get(0));
        assertEquals(new ScoreComparison.Row("Arte", 8, 5, -3), rows.get(1));
    }

    @Test
    void areaQueSoloExisteEnUnaEvaluacionCuentaComoCero() {
        List<ScoreComparison.Row> rows =
                ScoreComparison.compare(Map.of("Salud", 4), Map.of("Legal", 6));

        assertEquals(2, rows.size());
        assertEquals(new ScoreComparison.Row("Salud", 4, 0, -4), rows.get(0));
        assertEquals(new ScoreComparison.Row("Legal", 0, 6, 6), rows.get(1));
    }
}
