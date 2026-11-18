package pe.edu.upc.careerpath_ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.model.University;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.service.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/careers")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;
    private final UserService userService;
    private final UniversityService universityService;
    private final FavoriteService favoriteService;
    private final AIService aiService;

    @GetMapping
    public String list(Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("user", user);
        model.addAttribute("careers", careerService.findAll());
        return "careers/list";
    }

    @PostMapping("/compare")
    public String compare(@RequestParam(value = "ids", required = false) List<Long> ids,
                          Model model, RedirectAttributes redirectAttributes) {
        if (ids == null || ids.size() < 2) {
            redirectAttributes.addFlashAttribute("error",
                    "⚠️ Debes seleccionar al menos 2 carreras para comparar.");
            return "redirect:/careers";
        }
        List<Career> careersToCompare = careerService.findAllById(ids);
        
        if (careersToCompare.size() >= 2) {
            // 🆕 Le pasamos la lista completa a la IA
            String aiComparison = aiService.compareCareers(careersToCompare);
            model.addAttribute("aiComparison", aiComparison);
        }

        model.addAttribute("careers", careersToCompare);
        return "careers/compare";
    }

    @GetMapping("/compare")
    public String compareGet(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error",
                "⚠️ Por favor, selecciona las carreras desde la lista para compararlas.");
        return "redirect:/careers";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Career career = careerService.findByIdWithFreshDemand(id, 7);

        List<University> universities = (career.getArea() == null)
                ? List.of()
                : universityService.findByFacultiesContaining(career.getArea());

        if (!model.containsAttribute("courses")) {
            model.addAttribute("courses", new ArrayList<>());
        }

        boolean isFavorite = favoriteService.exists(user, career);

        model.addAttribute("user", user);
        model.addAttribute("career", career);
        model.addAttribute("universities", universities);
        model.addAttribute("isFavorite", isFavorite);
        
        return "careers/detail";
    }

    @GetMapping("/detail/{id}/ai-day")
    public String getDayInTheLife(@PathVariable Long id, Model model) {
        Career career = careerService.findById(id).orElseThrow();
        
        if (career.getAiDayInTheLife() == null || career.getAiDayInTheLife().isBlank()) {
            String aiText = aiService.generateDayInTheLife(career);
            
            if (aiText != null && !aiText.startsWith("⚠️")) {
                career.setAiDayInTheLife(aiText);
                careerService.save(career);
            }
            model.addAttribute("aiContent", aiText);
        } else {
            model.addAttribute("aiContent", career.getAiDayInTheLife());
        }
        
        return "fragments/ai-fragment :: response";
    }

    @GetMapping("/detail/{id}/ai-explain")
    public String getSimpleExplanation(@PathVariable Long id, Model model) {
        Career career = careerService.findById(id).orElseThrow();
        
        if (career.getAiSimpleExplanation() == null || career.getAiSimpleExplanation().isBlank()) {
            String aiText = aiService.explainSimply(career);
            
            if (aiText != null && !aiText.startsWith("⚠️")) {
                career.setAiSimpleExplanation(aiText);
                careerService.save(career);
            }
            model.addAttribute("aiContent", aiText);
        } else {
            model.addAttribute("aiContent", career.getAiSimpleExplanation());
        }
        
        return "fragments/ai-fragment :: response";
    }

    @GetMapping("/new")
    public String createForm(Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("career", new Career());
        return "careers/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute Career career, BindingResult result, Model model, Authentication auth) {
        if (result.hasErrors()) {
            User user = userService.findByUsername(auth.getName()).orElseThrow();
            model.addAttribute("user", user);
            return "careers/form";
        }
        careerService.save(career);
        return "redirect:/careers";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        Career career = careerService.findById(id).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("career", career);
        return "careers/form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute Career career, BindingResult result, Model model, Authentication auth) {
        if (result.hasErrors()) {
            User user = userService.findByUsername(auth.getName()).orElseThrow();
            model.addAttribute("user", user);
            return "careers/form";
        }
        careerService.update(id, career);
        return "redirect:/careers";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        careerService.delete(id);
        return "redirect:/careers";
    }
}