package pe.edu.upc.careerpath_ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.dto.TestAnswerDTO;
import pe.edu.upc.careerpath_ai.model.Question;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.service.QuestionService;
import pe.edu.upc.careerpath_ai.service.TestService;
import pe.edu.upc.careerpath_ai.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student/test")
@RequiredArgsConstructor
public class TestController {

    private final QuestionService questionService;
    private final TestService testService;
    private final UserService userService;

    // =====================================================
    // Mostrar formulario del test
    // =====================================================
    @GetMapping
    public String showTest(Model model) {
        model.addAttribute("questions", questionService.findAllOrdered());
        return "student/test";
    }

    // =====================================================
    // Procesar respuestas
    // =====================================================
    @PostMapping
    public String submitTest(@RequestParam Map<String, String> allParams,
                             Authentication auth) {
        List<Question> questions = questionService.findAllOrdered();
        List<TestAnswerDTO> answers = new ArrayList<>();

        for (Question q : questions) {
            String key = "answer_" + q.getId();
            String value = allParams.get(key);

            Integer score = 3; // valor por defecto
            if (value != null && !value.isBlank()) {
                try {
                    score = Integer.parseInt(value);
                } catch (NumberFormatException ignored) {
                    score = 3;
                }
            }

            TestAnswerDTO dto = new TestAnswerDTO();
            dto.setQuestionId(q.getId());
            dto.setScore(score);
            answers.add(dto);
        }

        User student = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var result = testService.processTest(student, answers);
        return "redirect:/student/results/" + result.getId();
    }
}