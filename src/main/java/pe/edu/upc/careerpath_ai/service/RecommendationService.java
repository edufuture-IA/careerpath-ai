package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.dto.RecommendationDTO;
import java.util.List;
import java.util.Map;

public interface RecommendationService {
    List<RecommendationDTO> recommend(Map<String, Integer> areaScores);
}