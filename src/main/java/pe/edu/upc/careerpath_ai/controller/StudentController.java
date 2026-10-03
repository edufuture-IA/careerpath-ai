package pe.edu.upc.careerpath_ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.service.TestService;
import pe.edu.upc.careerpath_ai.service.UserService;

import java.util.Map;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final UserService userService;
    private final TestService testService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("results", testService.findByStudent(user));
        return "student/dashboard";
    }

    @GetMapping("/results/{id}")
    public String results(@PathVariable Long id, Model model) {
        var result = testService.findById(id);
        model.addAttribute("result", result);
        model.addAttribute("areaScores", testService.parseAreaScores(result.getAreaScores()));
        model.addAttribute("recommended", testService.parseRecommendedCareers(result.getRecommendedCareers()));
        return "student/results";
    }

    // 🆕 Endpoint AJAX para generar token de compartir
    @PostMapping("/results/{id}/share")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, String>> share(@PathVariable Long id) {
        String token = testService.generateShareToken(id);
        return ResponseEntity.ok(Map.of("token", token, "url", "/share/" + token));
    }
}