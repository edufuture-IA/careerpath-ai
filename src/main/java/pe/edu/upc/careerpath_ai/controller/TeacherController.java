package pe.edu.upc.careerpath_ai.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.careerpath_ai.model.Role;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.model.User;
import pe.edu.upc.careerpath_ai.service.ExportService;
import pe.edu.upc.careerpath_ai.service.TestService;
import pe.edu.upc.careerpath_ai.service.UserService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TestService testService;
    private final UserService userService;
    private final ExportService exportService;

    private static final List<String> AREAS =
            List.of("Ingeniería", "Salud", "Negocios", "Arte", "Legal");

    // =====================================================
    // DASHBOARD con filtros y gráficos
    // =====================================================
    @GetMapping("/dashboard")
        @Transactional(readOnly = true)
        public String dashboard(Model model, Authentication auth,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false) String area) {

        User currentUser = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<TestResult> allResults = testService.findAll();
        List<User> students = userService.findByRole(Role.ESTUDIANTE);

        // Filtro por búsqueda
        if (search != null && !search.isBlank()) {
                String s = search.trim().toLowerCase();
                students = students.stream()
                        .filter(st -> st.getFullName().toLowerCase().contains(s)
                                || st.getUsername().toLowerCase().contains(s))
                        .collect(Collectors.toList());
        }

        // Filtro por área
        if (area != null && !area.isBlank()) {
                students = students.stream()
                        .filter(st -> testService.findByStudent(st).stream()
                                .anyMatch(r -> area.equalsIgnoreCase(r.getTopArea())))
                        .collect(Collectors.toList());
        }

        // Conteo de tests por alumno
        Map<Long, Long> resultCount = allResults.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStudent().getId(),
                        Collectors.counting()
                ));

        // 🆕 Última área por alumno (Map<studentId, area>)
        Map<Long, String> lastAreaByStudent = new HashMap<>();
        for (User s : students) {
                var studentResults = testService.findByStudent(s);
                lastAreaByStudent.put(s.getId(),
                        studentResults.isEmpty() ? "—" : studentResults.get(0).getTopArea());
        }

        // Distribución por área
        Map<String, Long> areaDistribution = new LinkedHashMap<>();
        for (String a : AREAS) areaDistribution.put(a, 0L);
        allResults.forEach(r -> {
                if (r.getTopArea() != null) {
                areaDistribution.merge(r.getTopArea(), 1L, Long::sum);
                }
        });

        // Tests por día
        Map<String, Long> testsByDay = new LinkedHashMap<>();
        allResults.stream()
                .filter(r -> r.getCompletedAt() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getCompletedAt().toLocalDate().toString(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> testsByDay.put(e.getKey(), e.getValue()));

        model.addAttribute("user", currentUser);
        model.addAttribute("students", students);
        model.addAttribute("results", allResults);
        model.addAttribute("resultCount", resultCount);
        model.addAttribute("lastAreaByStudent", lastAreaByStudent);   // 🆕
        model.addAttribute("areaDistribution", areaDistribution);
        model.addAttribute("testsByDay", testsByDay);
        model.addAttribute("availableAreas", AREAS);
        model.addAttribute("searchTerm", search);
        model.addAttribute("areaFilter", area);
        return "teacher/dashboard";
        }

    // =====================================================
    // DETALLE DE ALUMNO
    // =====================================================
    @GetMapping("/student/{id}")
    @Transactional(readOnly = true)
    public String studentDetail(@PathVariable Long id, Model model, Authentication auth) {
        User currentUser = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        User student = userService.findById(id);
        List<TestResult> results = testService.findByStudent(student);

        model.addAttribute("user", currentUser);
        model.addAttribute("student", student);
        model.addAttribute("results", results);
        return "teacher/student-detail";
    }

    // =====================================================
    // EXPORT INDIVIDUAL
    // =====================================================
    @GetMapping("/export/pdf/{id}")
    public void exportPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        TestResult result = testService.findById(id);
        exportService.exportTestResultToPdf(result, response);
    }

    @GetMapping("/export/word/{id}")
    public void exportWord(@PathVariable Long id, HttpServletResponse response) throws IOException {
        TestResult result = testService.findById(id);
        exportService.exportTestResultToWord(result, response);
    }

    // =====================================================
    // 🆕 EXPORT MASIVO (ZIP con múltiples PDFs)
    // =====================================================
    @PostMapping("/export/bulk")
    @Transactional(readOnly = true)
    public void exportBulk(@RequestParam("ids") List<Long> ids, HttpServletResponse response) throws IOException {
        if (ids == null || ids.isEmpty()) {
            response.sendError(400, "No se seleccionaron reportes");
            return;
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=reportes_vocacionales_" + System.currentTimeMillis() + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (Long id : ids) {
                TestResult result = testService.findById(id);
                byte[] pdf = exportService.generatePdfBytes(result);

                // Nombre seguro: nombre del alumno, sin caracteres raros
                String safeName = result.getStudent().getFullName()
                        .replaceAll("[^a-zA-Z0-9\\s]", "")
                        .replaceAll("\\s+", "_");
                String filename = "reporte_" + result.getId() + "_" + safeName + ".pdf";

                zos.putNextEntry(new ZipEntry(filename));
                zos.write(pdf);
                zos.closeEntry();
            }
        }
    }
}