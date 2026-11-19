package pe.edu.upc.careerpath_ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.careerpath_ai.model.Classroom;
import pe.edu.upc.careerpath_ai.model.Institution;
import pe.edu.upc.careerpath_ai.model.User;
import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByInstitution(Institution institution);

    // 🆕 Buscar el aula donde está un alumno
    @Query("SELECT c FROM Classroom c JOIN c.students s WHERE s = :student")
    Optional<Classroom> findByStudent(@Param("student") User student);

    // 🆕 Buscar el aula donde está un docente
    @Query("SELECT c FROM Classroom c JOIN c.teachers t WHERE t = :teacher")
    Optional<Classroom> findByTeacher(@Param("teacher") User teacher);
}