package pe.edu.upc.careerpath_ai.dto;

import pe.edu.upc.careerpath_ai.model.University;

public record UniversityDTO(Long id, String name, String website, String city,
                            String logoUrl, String description, String faculties) {
    public static UniversityDTO from(University u) {
        return new UniversityDTO(u.getId(), u.getName(), u.getWebsite(), u.getCity(),
                u.getLogoUrl(), u.getDescription(), u.getFaculties());
    }
}