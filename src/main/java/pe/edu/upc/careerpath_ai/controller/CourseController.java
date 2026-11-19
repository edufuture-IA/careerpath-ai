package pe.edu.upc.careerpath_ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.service.CourseService;
import pe.edu.upc.careerpath_ai.service.TestService;
import pe.edu.upc.careerpath_ai.service.UserService;

@Controller
@RequestMapping("/student/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final TestService testService;
    private final UserService userService;

    @GetMapping
    public String list(Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        var results = testService.findByStudent(user);

        String topArea = results.isEmpty() ? null : results.get(0).getTopArea();
        model.addAttribute("user", user);
        model.addAttribute("courses",
                topArea != null ? courseService.findByArea(topArea) : courseService.findAll());
        model.addAttribute("topArea", topArea);
        return "student/courses";
    }
}