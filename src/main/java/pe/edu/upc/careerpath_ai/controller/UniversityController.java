package pe.edu.upc.careerpath_ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.model.University;
import pe.edu.upc.careerpath_ai.service.UniversityService;

import java.util.List;

@Controller
@RequestMapping("/universities")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("universities", universityService.findAll());
        return "universities/list";
    }

    // MÉTODO PARA VER EL DETALLE
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        University university = universityService.findById(id)
                .orElseThrow(() -> new RuntimeException("Universidad no encontrada"));
        model.addAttribute("university", university);
        return "universities/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("university", new University());
        return "universities/form";
    }

    @PostMapping
    public String save(@ModelAttribute University university) {
        universityService.save(university);
        return "redirect:/universities";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        University university = universityService.findById(id)
                .orElseThrow(() -> new RuntimeException("Universidad no encontrada"));
        model.addAttribute("university", university);
        return "universities/form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute University university) {
        universityService.update(id, university);
        return "redirect:/universities";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        universityService.delete(id);
        return "redirect:/universities";
    }
}