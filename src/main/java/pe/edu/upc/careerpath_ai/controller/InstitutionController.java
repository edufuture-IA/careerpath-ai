package pe.edu.upc.careerpath_ai.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.dto.InstitutionForm;
import pe.edu.upc.careerpath_ai.model.*;
import pe.edu.upc.careerpath_ai.service.*;
import pe.edu.upc.careerpath_ai.service.impl.ExportServiceImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/institution")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;
    private final UserService userService;
    private final TestService testService;

    // =====================================================
    // REGISTRO
    // =====================================================
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("institutionForm", new InstitutionForm());
        return "institution/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute InstitutionForm form,
                           BindingResult result, Model model) {
        if (result.hasErrors()) return "institution/register";
        try {
            institutionService.register(form);
            return "redirect:/login?institutionCreated";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "institution/register";
        }
    }

    // =====================================================
    // DASHBOARD
    // =====================================================
    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(Model model, Authentication auth,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) Long classroomId) {

        User admin = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Long instId = admin.getInstitution().getId();
        Institution inst = institutionService.findById(instId);

        // 🆕 Filtrar alumnos
        List<User> students = institutionService.filterStudents(instId, search, classroomId);
        List<User> teachers = institutionService.teachersOf(instId);
        List<Classroom> classrooms = institutionService.classrooms(instId);

        // 🆕 Tests por alumno
        Map<Long, Integer> testCount = institutionService.testCountByStudent(students);

        // 🆕 Distribución por área
        Map<String, Long> areaDistribution = institutionService.areaDistribution(instId);

        // 🆕 Resumen por aula
        Map<Long, Map<String, Object>> classroomSummary =
                institutionService.classroomSummary(instId);

        // 🆕 Últimos resultados
        List<TestResult> latestResults = institutionService.latestResultsOf(instId);

        // Métricas globales
        long totalTests = students.stream()
                .mapToLong(s -> testService.findByStudent(s).size())
                .sum();

        long studentsWithTest = students.stream()
                .filter(s -> !testService.findByStudent(s).isEmpty())
                .count();

        model.addAttribute("institution", inst);
        model.addAttribute("students", students);
        model.addAttribute("teachers", teachers);
        model.addAttribute("classrooms", classrooms);
        model.addAttribute("testCount", testCount);
        model.addAttribute("areaDistribution", areaDistribution);
        model.addAttribute("classroomSummary", classroomSummary);
        model.addAttribute("latestResults", latestResults);
        model.addAttribute("totalTests", totalTests);
        model.addAttribute("studentsWithTest", studentsWithTest);
        model.addAttribute("searchTerm", search);
        model.addAttribute("classroomFilter", classroomId);
        return "institution/dashboard";
    }

    // =====================================================
    // AULAS
    // =====================================================
    @PostMapping("/classrooms")
    public String createClassroom(@RequestParam String name,
                                  @RequestParam(required = false) String grade,
                                  Authentication auth) {
        User admin = userService.findByUsername(auth.getName()).orElseThrow();
        institutionService.createClassroom(admin.getInstitution().getId(), name, grade);
        return "redirect:/institution/dashboard?classroomCreated";
    }

    @GetMapping("/classrooms/delete/{id}")
    public String deleteClassroom(@PathVariable Long id) {
        institutionService.deleteClassroom(id);
        return "redirect:/institution/dashboard?classroomDeleted";
    }

    @GetMapping("/classrooms/{id}")
    @Transactional(readOnly = true)
    public String classroomDetail(@PathVariable Long id, Model model, Authentication auth) {
        User admin = userService.findByUsername(auth.getName()).orElseThrow();
        Long instId = admin.getInstitution().getId();

        Classroom classroom = institutionService.classrooms(instId).stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aula no encontrada"));

        List<User> availableStudents = institutionService.studentsNotInAnyClassroom(instId);
        List<User> availableTeachers = institutionService.teachersNotInAnyClassroom(instId);

        model.addAttribute("classroom", classroom);
        model.addAttribute("availableStudents", availableStudents);
        model.addAttribute("availableTeachers", availableTeachers);
        return "institution/classroom-detail";
    }

    // =====================================================
    // ALUMNOS EN AULA
    // =====================================================
    @PostMapping("/classrooms/{id}/assign")
    public String assignStudent(@PathVariable Long id, @RequestParam Long studentId) {
        institutionService.assignStudentToClassroom(id, studentId);
        return "redirect:/institution/classrooms/" + id + "?assigned";
    }

    @GetMapping("/classrooms/{id}/remove/{studentId}")
    public String removeStudent(@PathVariable Long id, @PathVariable Long studentId) {
        institutionService.removeStudentFromClassroom(id, studentId);
        return "redirect:/institution/classrooms/" + id + "?removed";
    }

    // =====================================================
    // DOCENTES EN AULA
    // =====================================================
    @PostMapping("/classrooms/{id}/assign-teacher")
    public String assignTeacher(@PathVariable Long id, @RequestParam Long teacherId) {
        institutionService.assignTeacherToClassroom(id, teacherId);
        return "redirect:/institution/classrooms/" + id + "?teacherAssigned";
    }

    @GetMapping("/classrooms/{id}/remove-teacher/{teacherId}")
    public String removeTeacher(@PathVariable Long id, @PathVariable Long teacherId) {
        institutionService.removeTeacherFromClassroom(id, teacherId);
        return "redirect:/institution/classrooms/" + id + "?teacherRemoved";
    }

    // =====================================================
    // 🆕 DETALLE DE UN ALUMNO (desde el colegio)
    // =====================================================
    @GetMapping("/students/{id}")
    @Transactional(readOnly = true)
    public String studentDetail(@PathVariable Long id, Model model, Authentication auth) {
        User admin = userService.findByUsername(auth.getName()).orElseThrow();
        Long instId = admin.getInstitution().getId();

        User student = userService.findById(id);

        // Verificar que el alumno pertenece a la institución
        if (student.getInstitution() == null
                || !student.getInstitution().getId().equals(instId)) {
            return "redirect:/institution/dashboard?unauthorized";
        }

        List<TestResult> results = testService.findByStudent(student);
        Optional<Classroom> classroom = institutionService.findClassroomOfStudent(student);

        model.addAttribute("student", student);
        model.addAttribute("results", results);
        model.addAttribute("classroom", classroom.orElse(null));
        return "institution/student-detail";
    }

    // =====================================================
    // 🆕 COMPARAR AULAS
    // =====================================================
    @GetMapping("/classrooms/compare")
    @Transactional(readOnly = true)
    public String compareClassrooms(Model model, Authentication auth) {
        User admin = userService.findByUsername(auth.getName()).orElseThrow();
        Long instId = admin.getInstitution().getId();

        List<Classroom> classrooms = institutionService.classrooms(instId);
        Map<Long, Map<String, Object>> summary = institutionService.classroomSummary(instId);
        Map<Long, Map<String, Long>> distributions =
                institutionService.areaDistributionByClassroom(instId);

        model.addAttribute("classrooms", classrooms);
        model.addAttribute("summary", summary);
        model.addAttribute("distributions", distributions);
        return "institution/compare-classrooms";
    }

    // =====================================================
    // 🆕 EXPORTAR CSV (US19)
    // =====================================================
    @GetMapping("/export/csv")
    @Transactional(readOnly = true)
    public void exportCsv(Authentication auth, HttpServletResponse response) throws IOException {
        User admin = userService.findByUsername(auth.getName()).orElseThrow();
        Long instId = admin.getInstitution().getId();
        Institution inst = institutionService.findById(instId);
        List<User> students = institutionService.studentsOf(instId);
        List<Classroom> classrooms = institutionService.classrooms(instId);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=alumnos_" + inst.getName().replaceAll("\\s+","_") + ".csv");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        writer.write('\ufeff'); // BOM UTF-8 para Excel
        writer.println("Nombre,Correo,Aula,Grado,Area predominante,Carreras recomendadas,Tests realizados");

        for (User s : students) {
            String aulaName = "Sin aula";
            String aulaGrade = "—";
            for (Classroom c : classrooms) {
                if (c.getStudents().stream().anyMatch(st -> st.getId().equals(s.getId()))) {
                    aulaName = c.getName();
                    aulaGrade = c.getGrade() != null ? c.getGrade() : "—";
                    break;
                }
            }

            List<TestResult> results = testService.findByStudent(s);
            String area = results.isEmpty() ? "—" : results.get(0).getTopArea();
            String carreras = results.isEmpty() ? "—" : results.get(0).getRecommendedCareers();
            int numTests = results.size();

            writer.printf("%s,%s,%s,%s,%s,\"%s\",%d%n",
                    escapeCsv(s.getFullName()),
                    escapeCsv(s.getUsername()),
                    escapeCsv(aulaName),
                    escapeCsv(aulaGrade),
                    escapeCsv(area),
                    escapeCsv(carreras),
                    numTests);
        }
        writer.flush();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}