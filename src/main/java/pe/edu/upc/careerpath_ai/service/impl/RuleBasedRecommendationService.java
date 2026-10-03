package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.careerpath_ai.dto.RecommendationDTO;
import pe.edu.upc.careerpath_ai.repository.CareerRepository;
import pe.edu.upc.careerpath_ai.service.RecommendationService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RuleBasedRecommendationService implements RecommendationService {

    private final CareerRepository careerRepository;

    @Override
    public List<RecommendationDTO> recommend(Map<String, Integer> areaScores) {
        int total = areaScores.values().stream().mapToInt(Integer::intValue).sum();

        return careerRepository.findAll().stream()
                .map(c -> {
                    int areaScore = areaScores.getOrDefault(c.getArea(), 0);
                    double matchPct = total == 0 ? 0 : (areaScore * 100.0) / total;
                    return new RecommendationDTO(c.getId(), c.getName(), matchPct);
                })
                .sorted(Comparator.comparingDouble(RecommendationDTO::score).reversed())
                .limit(5)
                .toList();
    }
}