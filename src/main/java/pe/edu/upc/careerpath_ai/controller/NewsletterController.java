package pe.edu.upc.careerpath_ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.upc.careerpath_ai.dto.NewsletterRequest;
import pe.edu.upc.careerpath_ai.service.ContactService;

@Controller
@RequestMapping("/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final ContactService contactService;

    @PostMapping("/subscribe")
    public String subscribe(@Valid @ModelAttribute NewsletterRequest request,
                            RedirectAttributes ra) {
        try {
            contactService.subscribe(request.email());
            ra.addFlashAttribute("newsletterMsg", "¡Suscripción exitosa!");
        } catch (Exception e) {
            ra.addFlashAttribute("newsletterMsg", e.getMessage());
        }
        return "redirect:/";
    }
}