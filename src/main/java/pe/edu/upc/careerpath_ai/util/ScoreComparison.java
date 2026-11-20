package pe.edu.upc.careerpath_ai.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// US11: compara los puntajes por area de dos evaluaciones
public final class ScoreComparison {

    private ScoreComparison() {}

    public record Row(String area, int before, int after, int delta) {}

    public static List<Row> compare(Map<String, Integer> before, Map<String, Integer> after) {
        Set<String> areas = new LinkedHashSet<>(before.keySet());
        areas.addAll(after.keySet());
        List<Row> rows = new ArrayList<>();
        for (String area : areas) {
            int b = before.getOrDefault(area, 0);
            int a = after.getOrDefault(area, 0);
            rows.add(new Row(area, b, a, a - b));
        }
        return rows;
    }
}
