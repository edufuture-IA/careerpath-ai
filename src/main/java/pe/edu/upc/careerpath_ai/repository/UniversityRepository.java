package pe.edu.upc.careerpath_ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.careerpath_ai.model.University;
import java.util.List;

public interface UniversityRepository extends JpaRepository<University, Long> {
    // NUEVO MÉTODO
    List<University> findByFacultiesContaining(String faculties);
}