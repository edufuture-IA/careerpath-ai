package pe.edu.upc.careerpath_ai.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.dto.InstitutionForm;
import pe.edu.upc.careerpath_ai.model.*;
import pe.edu.upc.careerpath_ai.repository.*;
import pe.edu.upc.careerpath_ai.service.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final QuestionService questionService;
    private final InstitutionService institutionService;
    private final TestService testService;
    private final ActivityLogService activityLogService;

    private final TestResultRepository testResultRepository;
    private final CareerRepository careerRepository;
    private final ContactMessageRepository contactRepo;
    private final NewsletterSubscriberRepository newsRepo;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // =====================================================
    // DASHBOARD
    // =====================================================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalStudents", userService.findByRole(Role.ESTUDIANTE).size());
        model.addAttribute("totalTeachers", userService.findByRole(Role.DOCENTE).size());
        model.addAttribute("totalCareers", careerRepository.count());
        model.addAttribute("totalTests", testResultRepository.count());
        model.addAttribute("totalQuestions", questionService.count());
        model.addAttribute("contactMessages", contactRepo.count());
        model.addAttribute("newsletterCount", newsRepo.count());
        model.addAttribute("totalInstitutions", institutionService.findAll().size());
        model.addAttribute("totalLogs", activityLogService.count());
        return "admin/dashboard";
    }

    // =====================================================
    // USUARIOS
    // =====================================================
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("students", userService.findByRole(Role.ESTUDIANTE));
        model.addAttribute("teachers", userService.findByRole(Role.DOCENTE));
        return "admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, Authentication auth, HttpServletRequest request) {
        User user = userService.findById(id);
        activityLogService.log("DELETE_USER",
                "Eliminó usuario: " + user.getUsername(),
                auth.getName(), "ADMIN", request.getRemoteAddr());
        userRepository.deleteById(id);
        return "redirect:/admin/users?deleted";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "admin/user-edit";
    }

    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String fullName,
                             @RequestParam String username,
                             @RequestParam(required = false) Role role,
                             @RequestParam(required = false) String newPassword,
                             Authentication auth, HttpServletRequest request,
                             Model model) {

        User user = userService.findById(id);

        if (!username.equalsIgnoreCase(user.getUsername())
                && userRepository.existsByUsername(username)) {
            model.addAttribute("user", user);
            model.addAttribute("roles", Role.values());
            model.addAttribute("error", "Ese correo ya está en uso por otro usuario");
            return "admin/user-edit";
        }

        user.setFullName(fullName);
        user.setUsername(username);
        if (role != null) user.setRole(role);
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(user);
        activityLogService.log("UPDATE_USER",
                "Editó usuario: " + username,
                auth.getName(), "ADMIN", request.getRemoteAddr());
        return "redirect:/admin/users?updated";
    }

    // 🆕 Ver tests de un alumno
    @GetMapping("/students/{id}")
    @Transactional(readOnly = true)
    public String viewStudentTests(@PathVariable Long id, Model model) {
        User student = userService.findById(id);
        List<TestResult> results = testService.findByStudent(student);
        model.addAttribute("student", student);
        model.addAttribute("results", results);
        return "admin/student-detail";
    }

    // =====================================================
    // PREGUNTAS
    // =====================================================
    @GetMapping("/questions")
    public String questions(Model model) {
        model.addAttribute("questions", questionService.findAllOrdered());
        model.addAttribute("newQuestion", new Question());
        return "admin/questions";
    }

    @PostMapping("/questions")
    public String saveQuestion(@ModelAttribute Question question,
                               Authentication auth, HttpServletRequest request) {
        questionService.save(question);
        activityLogService.log("CREATE_QUESTION",
                "Creó pregunta: " + question.getText(),
                auth.getName(), "ADMIN", request.getRemoteAddr());
        return "redirect:/admin/questions?created";
    }

    @GetMapping("/questions/edit/{id}")
    public String editQuestionForm(@PathVariable Long id, Model model) {
        model.addAttribute("question", questionService.findById(id));
        return "admin/question-edit";
    }

    @PostMapping("/questions/edit/{id}")
    public String updateQuestion(@PathVariable Long id,
                                 @RequestParam String text,
                                 @RequestParam String area,
                                 @RequestParam Integer orderIndex,
                                 Authentication auth, HttpServletRequest request) {
        Question q = questionService.findById(id);
        q.setText(text);
        q.setArea(area);
        q.setOrderIndex(orderIndex);
        questionService.save(q);
        activityLogService.log("UPDATE_QUESTION",
                "Editó pregunta ID: " + id, auth.getName(), "ADMIN", request.getRemoteAddr());
        return "redirect:/admin/questions?updated";
    }

    @GetMapping("/questions/delete/{id}")
    public String deleteQuestion(@PathVariable Long id,
                                 Authentication auth, HttpServletRequest request) {
        questionService.delete(id);
        activityLogService.log("DELETE_QUESTION",
                "Eliminó pregunta ID: " + id, auth.getName(), "ADMIN", request.getRemoteAddr());
        return "redirect:/admin/questions?deleted";
    }

    // =====================================================
    // INSTITUCIONES
    // =====================================================
    @GetMapping("/institutions")
    public String institutions(Model model) {
        model.addAttribute("institutions", institutionService.findAll());
        return "admin/institutions";
    }

    @GetMapping("/institutions/new")
    public String newInstitutionForm(Model model) {
        model.addAttribute("institutionForm", new InstitutionForm());
        return "admin/institution-form";
    }

    @PostMapping("/institutions")
    public String createInstitution(@Valid @ModelAttribute("institutionForm") InstitutionForm form,
                                    BindingResult result, Model model,
                                    Authentication auth, HttpServletRequest request) {
        if (result.hasErrors()) return "admin/institution-form";
        try {
            institutionService.register(form);
            activityLogService.log("CREATE_INSTITUTION",
                    "Creó institución: " + form.getName(),
                    auth.getName(), "ADMIN", request.getRemoteAddr());
            return "redirect:/admin/institutions?created";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/institution-form";
        }
    }

    // 🆕 Editar institución
    @GetMapping("/institutions/edit/{id}")
    public String editInstitutionForm(@PathVariable Long id, Model model) {
        Institution inst = institutionService.findById(id);
        model.addAttribute("institution", inst);
        return "admin/institution-edit";
    }

    @PostMapping("/institutions/edit/{id}")
    public String updateInstitution(@PathVariable Long id,
                                    @RequestParam String name,
                                    @RequestParam String contactEmail,
                                    @RequestParam(required = false) String address,
                                    @RequestParam(required = false) String phone,
                                    Authentication auth, HttpServletRequest request,
                                    Model model) {

        Institution inst = institutionService.findById(id);
        inst.setName(name);
        inst.setContactEmail(contactEmail);
        inst.setAddress(address);
        inst.setPhone(phone);

        // Guardar usando el repo directamente
        institutionService.findAll(); // ya no reusamos register

        try {
            // Necesitamos guardar — usamos el service
            institutionService.updateInstitution(inst);
            activityLogService.log("UPDATE_INSTITUTION",
                    "Editó institución: " + name,
                    auth.getName(), "ADMIN", request.getRemoteAddr());
            return "redirect:/admin/institutions?updated";
        } catch (Exception e) {
            model.addAttribute("institution", inst);
            model.addAttribute("error", e.getMessage());
            return "admin/institution-edit";
        }
    }

    @GetMapping("/institutions/delete/{id}")
    public String deleteInstitution(@PathVariable Long id,
                                    Authentication auth, HttpServletRequest request) {
        Institution inst = institutionService.findById(id);
        activityLogService.log("DELETE_INSTITUTION",
                "Eliminó institución: " + inst.getName(),
                auth.getName(), "ADMIN", request.getRemoteAddr());
        institutionService.delete(id);
        return "redirect:/admin/institutions?deleted";
    }

    // =====================================================
    // MENSAJES DE CONTACTO
    // =====================================================
    @GetMapping("/messages")
    public String messages(Model model) {
        model.addAttribute("messages", contactRepo.findAllByOrderByCreatedAtDesc());
        return "admin/messages";
    }

    @PostMapping("/messages/mark/{id}")
    public String markMessageAttended(@PathVariable Long id) {
        contactRepo.findById(id).ifPresent(m -> {
            m.setAttended(true);
            contactRepo.save(m);
        });
        return "redirect:/admin/messages";
    }

    @PostMapping("/messages/delete/{id}")
    public String deleteMessage(@PathVariable Long id,
                                Authentication auth, HttpServletRequest request) {
        activityLogService.log("DELETE_MESSAGE",
                "Eliminó mensaje ID: " + id,
                auth.getName(), "ADMIN", request.getRemoteAddr());
        contactRepo.deleteById(id);
        return "redirect:/admin/messages";
    }

    // =====================================================
    // NEWSLETTER
    // =====================================================
    @GetMapping("/newsletter")
    public String newsletter(Model model) {
        model.addAttribute("subscribers", newsRepo.findAll());
        return "admin/newsletter";
    }

    @PostMapping("/newsletter/delete/{id}")
    public String deleteSubscriber(@PathVariable Long id,
                                   Authentication auth, HttpServletRequest request) {
        activityLogService.log("DELETE_SUBSCRIBER",
                "Eliminó suscriptor ID: " + id,
                auth.getName(), "ADMIN", request.getRemoteAddr());
        newsRepo.deleteById(id);
        return "redirect:/admin/newsletter";
    }

    // =====================================================
    // LOGS
    // =====================================================
    @GetMapping("/logs")
    public String logs(Model model) {
        model.addAttribute("logs", activityLogService.latest50());
        return "admin/logs";
    }
}