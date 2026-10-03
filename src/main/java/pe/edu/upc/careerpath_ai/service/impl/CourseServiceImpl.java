package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.careerpath_ai.model.Course;
import pe.edu.upc.careerpath_ai.repository.CourseRepository;
import pe.edu.upc.careerpath_ai.service.CourseService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override public List<Course> findAll() { return courseRepository.findAll(); }

    @Override public List<Course> findByArea(String area) { return courseRepository.findByArea(area); }

    @Override public Course save(Course course) { return courseRepository.save(course); }

    @Override public void delete(Long id) { courseRepository.deleteById(id); }

    // 🆕
    @Override
    public List<Course> findByAreaOrSkill(String area, String skill) {
        List<Course> all = courseRepository.findAll();
        return all.stream()
                .filter(c -> (area != null && !area.isBlank()
                                && c.getArea() != null
                                && c.getArea().equalsIgnoreCase(area))
                        || (skill != null && !skill.isBlank()
                                && c.getSkill() != null
                                && c.getSkill().toLowerCase().contains(skill.toLowerCase())))
                .toList();
    }
}