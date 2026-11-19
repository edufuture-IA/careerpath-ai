package pe.edu.upc.careerpath_ai.dto;

import pe.edu.upc.careerpath_ai.model.Course;

public record CourseDTO(Long id, String title, String provider, String url,
                        String area, String skill, Integer durationHours) {
    public static CourseDTO from(Course c) {
        return new CourseDTO(c.getId(), c.getTitle(), c.getProvider(), c.getUrl(),
                c.getArea(), c.getSkill(), c.getDurationHours());
    }
}