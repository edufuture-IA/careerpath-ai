package pe.edu.upc.careerpath_ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    public String add(@PathVariable Long careerId, Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        Career career = careerService.findById(careerId).orElseThrow();
        favoriteService.add(user, career);
        return "redirect:/careers/detail/" + careerId + "?favAdded";
    }

    @GetMapping("/remove/{careerId}")
    public String remove(@PathVariable Long careerId, Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        Career career = careerService.findById(careerId).orElseThrow();
        favoriteService.remove(user, career);
        return "redirect:/student/favorites";
    }
}