package pe.edu.upc.careerpath_ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.upc.careerpath_ai.exception.ConflictoException;
import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.service.CareerService;
import pe.edu.upc.careerpath_ai.service.FavoriteService;
import pe.edu.upc.careerpath_ai.service.UserService;

@Controller
@RequestMapping("/student/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final CareerService careerService;
    private final UserService userService;

    @GetMapping
    public String list(Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("favorites", favoriteService.findByUser(user));
        return "student/favorites";
    }

    @PostMapping("/add/{careerId}")
    public String add(@PathVariable Long careerId, Authentication auth, RedirectAttributes ra) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        Career career = careerService.findById(careerId).orElseThrow();
        
        try {
            favoriteService.add(user, career);
            ra.addFlashAttribute("success", "¡Carrera agregada a tus favoritos!");
        } catch (ConflictoException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/careers/detail/" + careerId;
    }

    @GetMapping("/remove/{careerId}")
    public String remove(@PathVariable Long careerId, Authentication auth, RedirectAttributes ra) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        Career career = careerService.findById(careerId).orElseThrow();
        favoriteService.remove(user, career);
        ra.addFlashAttribute("success", "Carrera eliminada de favoritos.");
        return "redirect:/careers/detail/" + careerId; // Modificado para que te devuelva a la misma vista
    }
}