package pe.edu.upc.careerpath_ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.dto.ProfileUpdateRequest;
import pe.edu.upc.careerpath_ai.model.Role;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.repository.UserRepository;
import pe.edu.upc.careerpath_ai.service.InstitutionService;
import pe.edu.upc.careerpath_ai.service.UserService;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final InstitutionService institutionService;

    @GetMapping
    @Transactional(readOnly = true)
    public String view(Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("user", user);

        // 🆕 Cargar institución y aula (solo lectura)
        if (user.getInstitution() != null) {
            model.addAttribute("institution", user.getInstitution());

            // Buscar aula según rol
            if (user.getRole() == Role.ESTUDIANTE) {
                institutionService.findClassroomOfStudent(user)
                        .ifPresent(c -> model.addAttribute("classroom", c));
            } else if (user.getRole() == Role.DOCENTE) {
                institutionService.findClassroomOfTeacher(user)
                        .ifPresent(c -> model.addAttribute("classroom", c));
            }
        }
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editForm(Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("form",
                new ProfileUpdateRequest(user.getFullName(), user.getUsername()));
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String update(@Valid @ModelAttribute("form") ProfileUpdateRequest form,
                         BindingResult result, Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();

        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "profile/edit";
        }

        if (!form.username().equalsIgnoreCase(user.getUsername())
                && userRepository.existsByUsername(form.username())) {
            model.addAttribute("error", "Ese correo ya está registrado por otro usuario");
            model.addAttribute("user", user);
            return "profile/edit";
        }

        user.setUsername(form.username());
        user.setFullName(form.fullName());
        userRepository.save(user);
        return "redirect:/profile?updated";
    }

    @PostMapping("/delete")
    public String delete(Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        userRepository.delete(user);
        return "redirect:/logout";
    }
}