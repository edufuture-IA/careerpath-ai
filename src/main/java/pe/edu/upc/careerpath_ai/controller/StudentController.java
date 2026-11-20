package pe.edu.upc.careerpath_ai.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.service.AIService;
import pe.edu.upc.careerpath_ai.service.ExportService;
import pe.edu.upc.careerpath_ai.service.TestService;
import pe.edu.upc.careerpath_ai.service.UserService;
import pe.edu.upc.careerpath_ai.util.ScoreComparison;

import java.io.IOException;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final UserService userService;
    private final TestService testService;
    private final AIService aiService;
    private final ExportService exportService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("results", testService.findByStudent(user));
        return "student/dashboard";
    }

    @GetMapping("/results/{id}")
    public String results(@PathVariable Long id, Model model, Authentication auth) {
        var result = testService.findById(id);
        
        User currentUser = userService.findByUsername(auth.getName()).orElseThrow();
        if (!result.getStudent().getId().equals(currentUser.getId())) {
            return "redirect:/student/dashboard";
        }

        model.addAttribute("result", result);
        model.addAttribute("areaScores", testService.parseAreaScores(result.getAreaScores()));
        model.addAttribute("recommended", testService.parseRecommendedCareers(result.getRecommendedCareers()));
        return "student/results";
    }

    // US11: comparar dos evaluaciones propias (la mas antigua queda como "antes")
    @GetMapping("/compare")
    public String compare(@RequestParam Long a, @RequestParam Long b,
                          Model model, Authentication auth) {
        if (a.equals(b)) {
            return "redirect:/student/dashboard?compareError";
        }
        var first = testService.findById(a);
        var second = testService.findById(b);
        User currentUser = userService.findByUsername(auth.getName()).orElseThrow();
        if (!first.getStudent().getId().equals(currentUser.getId())
                || !second.getStudent().getId().equals(currentUser.getId())) {
            return "redirect:/student/dashboard";
        }
        if (first.getCompletedAt().isAfter(second.getCompletedAt())) {
            var tmp = first; first = second; second = tmp;
        }
        model.addAttribute("before", first);
        model.addAttribute("after", second);
        model.addAttribute("rows", ScoreComparison.compare(
                testService.parseAreaScores(first.getAreaScores()),
                testService.parseAreaScores(second.getAreaScores())));
        return "student/compare";
    }

    // US10: el estudiante descarga su propio resultado en PDF
    @GetMapping("/results/{id}/pdf")
    public void downloadPdf(@PathVariable Long id, Authentication auth,
                            HttpServletResponse response) throws IOException {
        var result = testService.findById(id);
        User currentUser = userService.findByUsername(auth.getName()).orElseThrow();
        if (!result.getStudent().getId().equals(currentUser.getId())) {
            response.sendRedirect("/student/dashboard");
            return;
        }
        exportService.exportTestResultToPdf(result, response);
    }

    @GetMapping("/results/{id}/ai-roadmap")
    public String getRoadmap(@PathVariable Long id, Model model, Authentication auth) {
        var result = testService.findById(id);
        
        User currentUser = userService.findByUsername(auth.getName()).orElseThrow();
        if (!result.getStudent().getId().equals(currentUser.getId())) {
            return "redirect:/student/dashboard";
        }

        if (result.getAiRoadmap() == null || result.getAiRoadmap().isBlank()) {
            String aiText = aiService.generateRoadmap(result);
            
            // Solo guardamos si NO es un mensaje de error
            if (aiText != null && !aiText.startsWith("⚠️")) {
                result.setAiRoadmap(aiText);
                testService.save(result);
            }
            model.addAttribute("aiContent", aiText);
        } else {
            model.addAttribute("aiContent", result.getAiRoadmap());
        }

        return "fragments/ai-fragment :: response";
    }
}