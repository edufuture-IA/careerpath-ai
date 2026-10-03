package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.careerpath_ai.dto.RecommendationDTO;
import pe.edu.upc.careerpath_ai.dto.TestAnswerDTO;
import pe.edu.upc.careerpath_ai.model.Answer;
import pe.edu.upc.careerpath_ai.model.Question;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.repository.QuestionRepository;
import pe.edu.upc.careerpath_ai.repository.TestResultRepository;
import pe.edu.upc.careerpath_ai.service.RecommendationService;
import pe.edu.upc.careerpath_ai.service.TestService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final TestResultRepository testResultRepository;
    private final QuestionRepository questionRepository;
    private final RecommendationService recommendationService;

    @Override
    public TestResult processTest(User student, List<TestAnswerDTO> answerDTOs) {
        Map<String, Integer> areaScores = new HashMap<>();

        TestResult result = TestResult.builder()
                .student(student)
                .completedAt(LocalDateTime.now())
                .build();

        List<Answer> answers = new ArrayList<>();
        for (TestAnswerDTO dto : answerDTOs) {
            Question q = questionRepository.findById(dto.getQuestionId()).orElseThrow();
            areaScores.merge(q.getArea(), dto.getScore(), Integer::sum);

            answers.add(Answer.builder()
                    .testResult(result)
                    .question(q)
                    .score(dto.getScore())
                    .build());
        }
        result.setAnswers(answers);

        String topArea = areaScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("General");
        result.setTopArea(topArea);

        result.setAreaScores(areaScores.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(",")));

        List<RecommendationDTO> recs = recommendationService.recommend(areaScores);
        result.setRecommendedCareers(recs.stream()
                .map(RecommendationDTO::careerName)
                .collect(Collectors.joining(",")));

        return testResultRepository.save(result);
    }

    @Override
    public List<TestResult> findByStudent(User student) {
        return testResultRepository.findByStudentOrderByCompletedAtDesc(student);
    }

    @Override
    public List<TestResult> findAll() {
        return testResultRepository.findAllByOrderByCompletedAtDesc();
    }

    @Override
    public TestResult findById(Long id) {
        return testResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resultado no encontrado"));
    }

    @Override
    public Map<String, Integer> parseAreaScores(String csv) {
        Map<String, Integer> map = new LinkedHashMap<>();
        if (csv == null || csv.isBlank()) return map;
        for (String pair : csv.split(",")) {
            String[] kv = pair.split(":");
            if (kv.length == 2) map.put(kv[0], Integer.parseInt(kv[1].trim()));
        }
        return map;
    }

    @Override
    public List<String> parseRecommendedCareers(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(",")).map(String::trim).toList();
    }

    // =====================================================
    // 🆕 COMPARTIR
    // =====================================================
    @Override
    public String generateShareToken(Long resultId) {
        TestResult result = testResultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Resultado no encontrado"));

        // Si ya tiene token, lo devolvemos
        if (result.getShareToken() != null && !result.getShareToken().isBlank()) {
            return result.getShareToken();
        }

        String token = UUID.randomUUID().toString();
        result.setShareToken(token);
        testResultRepository.save(result);
        return token;
    }

    @Override
    public Optional<TestResult> findByShareToken(String token) {
        return testResultRepository.findByShareToken(token);
    }
}