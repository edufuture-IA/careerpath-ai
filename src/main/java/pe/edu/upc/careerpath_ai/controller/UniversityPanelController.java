package pe.edu.upc.careerpath_ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.model.University;
import pe.edu.upc.careerpath_ai.service.UniversityService;
import pe.edu.upc.careerpath_ai.service.UserService;

// US20 y US21: panel de la cuenta con rol UNIVERSIDAD.
// La universidad sale siempre del usuario logueado, nunca de un id en la URL,
// asi no puede tocar los datos de otra institucion.
@Controller
@RequestMapping("/university")
@RequiredArgsConstructor
public class UniversityPanelController {

    private final UserService userService;
    private final UniversityService universityService;

    private University ownUniversity(Authentication auth) {
        return userService.findByUsername(auth.getName()).orElseThrow().getUniversity();
    }

    @GetMapping("/panel")
    public String panel(Authentication auth, Model model) {
        model.addAttribute("university", ownUniversity(auth));
        return "university/panel";
    }

    @PostMapping("/panel")
    public String save(@RequestParam(defaultValue = "") String website,
                       @RequestParam(defaultValue = "") String city,
                       @RequestParam(defaultValue = "") String logoUrl,
                       @RequestParam(defaultValue = "") String description,
                       @RequestParam(defaultValue = "") String faculties,
                       Authentication auth) {
        University own = ownUniversity(auth);
        if (own == null) {
            return "redirect:/university/panel";
        }
        universityService.updateOwnInfo(own.getId(), website, city, logoUrl, description, faculties);
        return "redirect:/university/panel?saved";
    }

    @GetMapping("/stats")
    public String stats(Authentication auth, Model model) {
        University own = ownUniversity(auth);
        model.addAttribute("university", own);
        if (own != null) {
            model.addAttribute("stats", universityService.affinityStats(own));
        }
        return "university/stats";
    }
}
