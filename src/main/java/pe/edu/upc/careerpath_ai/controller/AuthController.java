package pe.edu.upc.careerpath_ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.dto.RegisterForm;
import pe.edu.upc.careerpath_ai.model.Role;
import pe.edu.upc.careerpath_ai.service.UserService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() { return "auth/login"; }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        model.addAttribute("roles", new Role[]{Role.ESTUDIANTE, Role.DOCENTE});
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterForm registerForm,
                           BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", new Role[]{Role.ESTUDIANTE, Role.DOCENTE});
            return "auth/register";
        }
        try {
            userService.register(registerForm);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", new Role[]{Role.ESTUDIANTE, Role.DOCENTE});
            return "auth/register";
        }
        return "redirect:/login?registered";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        String role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .findFirst()
                .orElse("ROLE_ESTUDIANTE");

        return switch (role) {
            case "ROLE_ADMIN"   -> "redirect:/admin/dashboard";
            case "ROLE_DOCENTE" -> "redirect:/teacher/dashboard";
            case "ROLE_COLEGIO" -> "redirect:/institution/dashboard";
            default             -> "redirect:/student/dashboard";
        };
    }
}