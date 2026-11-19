package pe.edu.upc.careerpath_ai.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.dto.AIChatRequest;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.service.AIService;
import pe.edu.upc.careerpath_ai.service.TestService;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final TestService testService;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResult(@RequestBody AIChatRequest request) {
        TestResult result = testService.findById(request.getTestResultId());
        String analysis = aiService.generateAnalysis(result);
        return ResponseEntity.ok(Map.of("analysis", analysis));
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody AIChatRequest request) {
        TestResult result = testService.findById(request.getTestResultId());
        String response = aiService.chatWithAI(result, request.getPrompt());
        return ResponseEntity.ok(Map.of("response", response));
    }
}