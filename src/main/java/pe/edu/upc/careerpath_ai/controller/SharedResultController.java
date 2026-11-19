package pe.edu.upc.careerpath_ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.service.TestService;

@Controller
@RequestMapping("/share")
@RequiredArgsConstructor
public class SharedResultController {

    private final TestService testService;

    @GetMapping("/{token}")
    public String viewShared(@PathVariable String token, Model model) {
        TestResult result = testService.findByShareToken(token)
                .orElse(null);

        if (result == null) {
            return "shared/not-found";
        }

        model.addAttribute("result", result);
        model.addAttribute("areaScores", testService.parseAreaScores(result.getAreaScores()));
        model.addAttribute("recommended", testService.parseRecommendedCareers(result.getRecommendedCareers()));
        return "shared/result";
    }
}