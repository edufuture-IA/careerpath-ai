package pe.edu.upc.careerpath_ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.service.CareerService;

import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/careers/compare")
@RequiredArgsConstructor
public class ComparatorController {

    private final CareerService careerService;

    @GetMapping
    public String selectForm(Model model) {
        model.addAttribute("careers", careerService.findAll());
        return "careers/compare-select";
    }

    @PostMapping
    public String compare(@RequestParam("ids") List<Long> ids, Model model) {
        List<Career> careers = ids.stream()
                .map(id -> careerService.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        model.addAttribute("careers", careers);
        return "careers/compare-result";
    }
}