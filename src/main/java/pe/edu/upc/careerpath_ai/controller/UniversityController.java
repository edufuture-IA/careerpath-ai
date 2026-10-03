package pe.edu.upc.careerpath_ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.exception.RecursoNoEncontradoException;
import pe.edu.upc.careerpath_ai.model.University;
import pe.edu.upc.careerpath_ai.service.UniversityService;

@Controller
@RequestMapping("/universities")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    // =====================================================
    // LISTAR — público (todos pueden ver)
    // =====================================================
    @GetMapping
    public String list(Model model) {
        model.addAttribute("universities", universityService.findAll());
        return "universities/list";
    }

    // =====================================================
    // DETALLE — público (todos pueden ver)
    // =====================================================
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("university",
                universityService.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Universidad no encontrada")));
        return "universities/detail";
    }

    // =====================================================
    // CREAR — SOLO ADMIN
    // =====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("university", new University());
        return "universities/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String save(@ModelAttribute University u) {
        universityService.save(u);
        return "redirect:/universities?created";
    }

    // =====================================================
    // EDITAR — SOLO ADMIN
    // =====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        University university = universityService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Universidad no encontrada"));
        model.addAttribute("university", university);
        return "universities/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute University u) {
        universityService.update(id, u);
        return "redirect:/universities?updated";
    }

    // =====================================================
    // ELIMINAR — SOLO ADMIN
    // =====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        universityService.delete(id);
        return "redirect:/universities?deleted";
    }
}