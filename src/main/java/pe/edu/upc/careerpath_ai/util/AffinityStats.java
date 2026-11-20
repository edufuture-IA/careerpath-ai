package pe.edu.upc.careerpath_ai.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// US21: cuantas evaluaciones recomiendan cada carrera de la universidad.
// Solo devuelve conteos agrupados: nunca nombres ni datos personales.
public final class AffinityStats {

    private AffinityStats() {}

    public record Row(String career, long evaluations, double percent) {}

    public record Result(long totalEvaluations, List<Row> rows) {}

    public static Result compute(Collection<String> careerNames, List<String> recommendedLists) {
        long total = recommendedLists.size();
        List<Row> rows = new ArrayList<>();
        for (String career : careerNames) {
            String wanted = career.trim().toLowerCase(Locale.ROOT);
            long count = 0;
            for (String csv : recommendedLists) {
                if (contains(csv, wanted)) count++;
            }
            double percent = total == 0 ? 0 : Math.round(count * 1000.0 / total) / 10.0;
            rows.add(new Row(career, count, percent));
        }
        rows.sort(Comparator.comparingLong(Row::evaluations).reversed()
                .thenComparing(Row::career));
        return new Result(total, rows);
    }

    private static boolean contains(String csv, String wantedLowerCase) {
        if (csv == null || csv.isBlank()) return false;
        Set<String> names = new HashSet<>();
        for (String part : csv.split(",")) {
            names.add(part.trim().toLowerCase(Locale.ROOT));
        }
        return names.contains(wantedLowerCase);
    }
}
