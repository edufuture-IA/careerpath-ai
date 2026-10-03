package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.careerpath_ai.dto.InstitutionForm;
import pe.edu.upc.careerpath_ai.exception.ConflictoException;
import pe.edu.upc.careerpath_ai.exception.RecursoNoEncontradoException;
import pe.edu.upc.careerpath_ai.model.*;
import pe.edu.upc.careerpath_ai.repository.*;
import pe.edu.upc.careerpath_ai.service.InstitutionService;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final TestResultRepository testResultRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RNG = new SecureRandom();
    private static final List<String> AREAS =
            List.of("Ingeniería", "Salud", "Negocios", "Arte", "Legal");

    // =====================================================
    // REGISTRO
    // =====================================================
    @Override
    @Transactional
    public Institution register(InstitutionForm form) {
        if (institutionRepository.existsByName(form.getName()))
            throw new ConflictoException("Ya existe una institución con ese nombre");
        if (institutionRepository.existsByContactEmail(form.getContactEmail()))
            throw new ConflictoException("Ya existe una institución con ese correo");

        Institution inst = Institution.builder()
                .name(form.getName())
                .contactEmail(form.getContactEmail())
                .address(form.getAddress())
                .phone(form.getPhone())
                .invitationCode(generateCode())
                .build();
        institutionRepository.save(inst);

        User admin = User.builder()
                .username(form.getContactEmail())
                .password(passwordEncoder.encode(form.getAdminPassword()))
                .fullName(form.getAdminFullName())
                .role(Role.COLEGIO)
                .institution(inst)
                .build();
        userRepository.save(admin);

        return inst;
    }

    // =====================================================
    // BÚSQUEDAS
    // =====================================================
    @Override
    public Institution findByInvitationCode(String code) {
        return institutionRepository.findByInvitationCode(code)
                .orElseThrow(() -> new RecursoNoEncontradoException("Código inválido"));
    }

    @Override
    public Institution findById(Long id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Institución no encontrada"));
    }

    @Override
    public List<Classroom> classrooms(Long institutionId) {
        return classroomRepository.findByInstitution(findById(institutionId));
    }

    @Override
    public Classroom createClassroom(Long institutionId, String name, String grade) {
        Institution inst = findById(institutionId);
        return classroomRepository.save(
                Classroom.builder().name(name).grade(grade).institution(inst).build());
    }

    // =====================================================
    // ALUMNOS
    // =====================================================
    @Override
    public List<User> studentsOf(Long institutionId) {
        return userRepository.findByRole(Role.ESTUDIANTE).stream()
                .filter(s -> s.getInstitution() != null
                        && s.getInstitution().getId().equals(institutionId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Classroom assignStudentToClassroom(Long classroomId, Long studentId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Aula no encontrada"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado"));

        if (student.getRole() != Role.ESTUDIANTE)
            throw new ConflictoException("Solo se pueden asignar estudiantes a un aula");

        if (student.getInstitution() == null
                || !student.getInstitution().getId().equals(classroom.getInstitution().getId()))
            throw new ConflictoException("El alumno no pertenece a esta institución");

        boolean alreadyIn = classroom.getStudents().stream()
                .anyMatch(s -> s.getId().equals(studentId));
        if (!alreadyIn) {
            classroom.getStudents().add(student);
            classroomRepository.save(classroom);
        }
        return classroom;
    }

    @Override
    @Transactional
    public void removeStudentFromClassroom(Long classroomId, Long studentId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Aula no encontrada"));
        classroom.getStudents().removeIf(s -> s.getId().equals(studentId));
        classroomRepository.save(classroom);
    }

    @Override
    public List<User> studentsNotInAnyClassroom(Long institutionId) {
        List<User> allStudents = studentsOf(institutionId);
        List<Classroom> classrooms = classrooms(institutionId);

        List<Long> studentsInClassrooms = classrooms.stream()
                .flatMap(c -> c.getStudents().stream())
                .map(User::getId)
                .toList();

        return allStudents.stream()
                .filter(s -> !studentsInClassrooms.contains(s.getId()))
                .toList();
    }

    @Override
    public void deleteClassroom(Long classroomId) {
        classroomRepository.deleteById(classroomId);
    }

    // =====================================================
    // DOCENTES
    // =====================================================
    @Override
    public List<User> teachersOf(Long institutionId) {
        return userRepository.findByRole(Role.DOCENTE).stream()
                .filter(t -> t.getInstitution() != null
                        && t.getInstitution().getId().equals(institutionId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Classroom assignTeacherToClassroom(Long classroomId, Long teacherId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Aula no encontrada"));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Docente no encontrado"));

        if (teacher.getRole() != Role.DOCENTE)
            throw new ConflictoException("Solo se pueden asignar docentes a un aula");

        if (teacher.getInstitution() == null
                || !teacher.getInstitution().getId().equals(classroom.getInstitution().getId()))
            throw new ConflictoException("El docente no pertenece a esta institución");

        boolean alreadyIn = classroom.getTeachers().stream()
                .anyMatch(t -> t.getId().equals(teacherId));
        if (!alreadyIn) {
            classroom.getTeachers().add(teacher);
            classroomRepository.save(classroom);
        }
        return classroom;
    }

    @Override
    @Transactional
    public void removeTeacherFromClassroom(Long classroomId, Long teacherId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Aula no encontrada"));
        classroom.getTeachers().removeIf(t -> t.getId().equals(teacherId));
        classroomRepository.save(classroom);
    }

    @Override
    public List<User> teachersNotInAnyClassroom(Long institutionId) {
        List<User> allTeachers = teachersOf(institutionId);
        List<Classroom> classrooms = classrooms(institutionId);

        List<Long> teachersInClassrooms = classrooms.stream()
                .flatMap(c -> c.getTeachers().stream())
                .map(User::getId)
                .toList();

        return allTeachers.stream()
                .filter(t -> !teachersInClassrooms.contains(t.getId()))
                .toList();
    }

    // =====================================================
    // PERFIL
    // =====================================================
    @Override
    public Optional<Classroom> findClassroomOfStudent(User student) {
        return classroomRepository.findByStudent(student);
    }

    @Override
    public Optional<Classroom> findClassroomOfTeacher(User teacher) {
        return classroomRepository.findByTeacher(teacher);
    }

    // =====================================================
    // ADMIN
    // =====================================================
    @Override
    public List<Institution> findAll() {
        return institutionRepository.findAll();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getInstitution() != null
                        && u.getInstitution().getId().equals(id))
                .toList();
        users.forEach(u -> u.setInstitution(null));
        userRepository.saveAll(users);
        institutionRepository.deleteById(id);
    }

    // 🆕 EDITAR INSTITUCIÓN
    @Override
    @Transactional
    public Institution updateInstitution(Institution institution) {
        Institution existing = institutionRepository.findById(institution.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Institución no encontrada"));

        // Validar nombre duplicado (si cambió)
        if (!existing.getName().equalsIgnoreCase(institution.getName())
                && institutionRepository.existsByName(institution.getName())) {
            throw new ConflictoException("Ya existe otra institución con ese nombre");
        }

        // Validar correo duplicado (si cambió)
        if (!existing.getContactEmail().equalsIgnoreCase(institution.getContactEmail())
                && institutionRepository.existsByContactEmail(institution.getContactEmail())) {
            throw new ConflictoException("Ya existe otra institución con ese correo");
        }

        existing.setName(institution.getName());
        existing.setContactEmail(institution.getContactEmail());
        existing.setAddress(institution.getAddress());
        existing.setPhone(institution.getPhone());

        return institutionRepository.save(existing);
    }

    // =====================================================
    // FUNCIONES B2B
    // =====================================================
    @Override
    public List<User> filterStudents(Long institutionId, String search, Long classroomId) {
        List<User> students = studentsOf(institutionId);

        if (search != null && !search.isBlank()) {
            String s = search.trim().toLowerCase();
            students = students.stream()
                    .filter(st -> st.getFullName().toLowerCase().contains(s)
                            || st.getUsername().toLowerCase().contains(s))
                    .toList();
        }

        if (classroomId != null) {
            Classroom classroom = classroomRepository.findById(classroomId).orElse(null);
            if (classroom != null) {
                List<Long> idsInClassroom = classroom.getStudents().stream()
                        .map(User::getId).toList();
                students = students.stream()
                        .filter(st -> idsInClassroom.contains(st.getId()))
                        .toList();
            }
        }
        return students;
    }

    @Override
    public Map<Long, Integer> testCountByStudent(List<User> students) {
        Map<Long, Integer> map = new HashMap<>();
        for (User s : students) {
            map.put(s.getId(), testResultRepository.findByStudentOrderByCompletedAtDesc(s).size());
        }
        return map;
    }

    @Override
    public Map<String, Long> areaDistribution(Long institutionId) {
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (String a : AREAS) distribution.put(a, 0L);

        List<User> students = studentsOf(institutionId);
        for (User s : students) {
            List<TestResult> results = testResultRepository.findByStudentOrderByCompletedAtDesc(s);
            for (TestResult r : results) {
                if (r.getTopArea() != null) {
                    distribution.merge(r.getTopArea(), 1L, Long::sum);
                }
            }
        }
        return distribution;
    }

    @Override
    public Map<Long, Map<String, Long>> areaDistributionByClassroom(Long institutionId) {
        Map<Long, Map<String, Long>> result = new HashMap<>();
        List<Classroom> classrooms = classrooms(institutionId);

        for (Classroom c : classrooms) {
            Map<String, Long> dist = new LinkedHashMap<>();
            for (String a : AREAS) dist.put(a, 0L);

            for (User s : c.getStudents()) {
                List<TestResult> results = testResultRepository.findByStudentOrderByCompletedAtDesc(s);
                for (TestResult r : results) {
                    if (r.getTopArea() != null) {
                        dist.merge(r.getTopArea(), 1L, Long::sum);
                    }
                }
            }
            result.put(c.getId(), dist);
        }
        return result;
    }

    @Override
    public Map<Long, Map<String, Object>> classroomSummary(Long institutionId) {
        Map<Long, Map<String, Object>> summary = new HashMap<>();
        List<Classroom> classrooms = classrooms(institutionId);

        for (Classroom c : classrooms) {
            Map<String, Object> info = new HashMap<>();

            long totalStudents = c.getStudents().size();
            long studentsWithTest = c.getStudents().stream()
                    .filter(s -> !testResultRepository.findByStudentOrderByCompletedAtDesc(s).isEmpty())
                    .count();

            long totalTests = c.getStudents().stream()
                    .mapToLong(s -> testResultRepository.findByStudentOrderByCompletedAtDesc(s).size())
                    .sum();

            Map<String, Long> dist = new LinkedHashMap<>();
            for (String a : AREAS) dist.put(a, 0L);
            for (User s : c.getStudents()) {
                testResultRepository.findByStudentOrderByCompletedAtDesc(s).forEach(r -> {
                    if (r.getTopArea() != null) dist.merge(r.getTopArea(), 1L, Long::sum);
                });
            }
            String topArea = dist.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .filter(e -> e.getValue() > 0)
                    .map(Map.Entry::getKey)
                    .orElse("—");

            info.put("name", c.getName());
            info.put("grade", c.getGrade());
            info.put("studentCount", totalStudents);
            info.put("teacherCount", c.getTeachers().size());
            info.put("studentsWithTest", studentsWithTest);
            info.put("totalTests", totalTests);
            info.put("topArea", topArea);
            info.put("completion", totalStudents == 0 ? 0 :
                    (int) Math.round((studentsWithTest * 100.0) / totalStudents));

            summary.put(c.getId(), info);
        }
        return summary;
    }

    @Override
    public List<TestResult> latestResultsOf(Long institutionId) {
        List<User> students = studentsOf(institutionId);
        return students.stream()
                .flatMap(s -> testResultRepository.findByStudentOrderByCompletedAtDesc(s).stream())
                .sorted(Comparator.comparing(TestResult::getCompletedAt).reversed())
                .limit(10)
                .toList();
    }

    private String generateCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++)
                sb.append(CODE_CHARS.charAt(RNG.nextInt(CODE_CHARS.length())));
            code = sb.toString();
        } while (institutionRepository.findByInvitationCode(code).isPresent());
        return code;
    }
}