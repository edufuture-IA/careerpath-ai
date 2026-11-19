package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.dto.InstitutionForm;
import pe.edu.upc.careerpath_ai.model.Classroom;
import pe.edu.upc.careerpath_ai.model.Institution;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.model.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InstitutionService {
    Institution register(InstitutionForm form);
    Institution findByInvitationCode(String code);
    Institution findById(Long id);
    List<Classroom> classrooms(Long institutionId);
    Classroom createClassroom(Long institutionId, String name, String grade);

    // Alumnos
    List<User> studentsOf(Long institutionId);
    Classroom assignStudentToClassroom(Long classroomId, Long studentId);
    void removeStudentFromClassroom(Long classroomId, Long studentId);
    List<User> studentsNotInAnyClassroom(Long institutionId);
    void deleteClassroom(Long classroomId);

    // Docentes
    List<User> teachersOf(Long institutionId);
    Classroom assignTeacherToClassroom(Long classroomId, Long teacherId);
    void removeTeacherFromClassroom(Long classroomId, Long teacherId);
    List<User> teachersNotInAnyClassroom(Long institutionId);

    // Perfil
    Optional<Classroom> findClassroomOfStudent(User student);
    Optional<Classroom> findClassroomOfTeacher(User teacher);

    // Admin
    List<Institution> findAll();
    void delete(Long id);
    Institution updateInstitution(Institution institution);   // 🆕 AGREGAR

    // Funciones B2B
    List<User> filterStudents(Long institutionId, String search, Long classroomId);
    Map<Long, Integer> testCountByStudent(List<User> students);
    Map<String, Long> areaDistribution(Long institutionId);
    Map<Long, Map<String, Long>> areaDistributionByClassroom(Long institutionId);
    Map<Long, Map<String, Object>> classroomSummary(Long institutionId);
    List<TestResult> latestResultsOf(Long institutionId);
}