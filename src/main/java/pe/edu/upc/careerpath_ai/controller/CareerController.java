package pe.edu.upc.careerpath_ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.service.CareerService;
import pe.edu.upc.careerpath_ai.service.CourseService;
import pe.edu.upc.careerpath_ai.service.UniversityService;
import pe.edu.upc.careerpath_ai.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/careers")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;
    private final UserService userService;
    private final UniversityService universityService;
    private final CourseService courseService;

    // =====================================================
    // LISTAR + BUSCAR
    // =====================================================
    @GetMapping
    public String list(Model model, Authentication auth,
                       @RequestParam(required = false) String search) {

        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Career> careers = careerService.findAll();

        // 🆕 Filtro por nombre
        if (search != null && !search.isBlank()) {
            String s = search.trim().toLowerCase();
            careers = careers.stream()
                    .filter(c -> c.getName().toLowerCase().contains(s)
                            || (c.getArea() != null && c.getArea().toLowerCase().contains(s)))
                    .toList();
        }

        model.addAttribute("user", user);
        model.addAttribute("careers", careers);
        model.addAttribute("searchTerm", search);
        return "careers/list";
    }

    // =====================================================
    // CREAR
    // =====================================================
    @GetMapping("/new")
    public String createForm(Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("user", user);
        model.addAttribute("career", new Career());
        return "careers/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute Career career,
                       BindingResult result, Model model, Authentication auth) {
        if (result.hasErrors()) {
            User user = userService.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            model.addAttribute("user", user);
            return "careers/form";
        }
        careerService.save(career);
        return "redirect:/careers";
    }

    // =====================================================
    // EDITAR
    // =====================================================
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Career career = careerService.findById(id)
                .orElseThrow(() -> new RuntimeException("Carrera no encontrada"));
        model.addAttribute("user", user);
        model.addAttribute("career", career);
        return "careers/form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Career career,
                         BindingResult result, Model model, Authentication auth) {
        if (result.hasErrors()) {
            User user = userService.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            model.addAttribute("user", user);
            return "careers/form";
        }
        careerService.update(id, career);
        return "redirect:/careers";
    }

    // =====================================================
    // ELIMINAR
    // =====================================================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        careerService.delete(id);
        return "redirect:/careers";
    }

    // =====================================================
    // DETALLE + universidades + cursos recomendados
    // =====================================================
    @GetMapping("/detail/{id}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable Long id, Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Career career = careerService.findById(id)
                .orElseThrow(() -> new RuntimeException("Carrera no encontrada"));

        // 🆕 Universidades que ofrecen esta área
        List<pe.edu.upc.careerpath_ai.model.University> universities =
                universityService.findByFacultiesContaining(career.getArea());

        // 🆕 Cursos relacionados (por área y por skills)
        List<pe.edu.upc.careerpath_ai.model.Course> courses =
                courseService.findByAreaOrSkill(career.getArea(), career.getRequiredSkills());

        // Limitar a los primeros 4 cursos
        if (courses.size() > 4) courses = courses.subList(0, 4);

        model.addAttribute("user", user);
        model.addAttribute("career", career);
        model.addAttribute("universities", universities);
        model.addAttribute("courses", courses);
        return "careers/detail";
    }
}