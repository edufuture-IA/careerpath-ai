package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.dto.TestAnswerDTO;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.model.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TestService {
    TestResult processTest(User student, List<TestAnswerDTO> answers);
    List<TestResult> findByStudent(User student);
    List<TestResult> findAll();
    TestResult findById(Long id);
    Map<String, Integer> parseAreaScores(String csv);
    List<String> parseRecommendedCareers(String csv);

    // 🆕 Compartir
    String generateShareToken(Long resultId);
    Optional<TestResult> findByShareToken(String token);
}