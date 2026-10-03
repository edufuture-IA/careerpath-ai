package pe.edu.upc.careerpath_ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.careerpath_ai.model.Career;

public interface CareerRepository extends JpaRepository<Career, Long> {
}