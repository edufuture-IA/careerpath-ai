package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.model.Course;
import java.util.List;

public interface CourseService {
    List<Course> findAll();
    List<Course> findByArea(String area);
    Course save(Course course);
    void delete(Long id);

    // 🆕 Buscar por área Y skill
    List<Course> findByAreaOrSkill(String area, String skill);
}