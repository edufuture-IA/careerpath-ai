package pe.edu.upc.careerpath_ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.careerpath_ai.model.Course;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByArea(String area);
}