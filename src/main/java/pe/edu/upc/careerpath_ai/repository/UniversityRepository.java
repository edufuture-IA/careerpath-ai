package pe.edu.upc.careerpath_ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.careerpath_ai.model.University;

public interface UniversityRepository extends JpaRepository<University, Long> {
}