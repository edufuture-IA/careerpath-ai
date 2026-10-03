package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.model.University;
import java.util.List;
import java.util.Optional;

public interface UniversityService {
    List<University> findAll();
    Optional<University> findById(Long id);
    University save(University u);
    University update(Long id, University u);
    void delete(Long id);

    // 🆕 Buscar universidades por facultad/área
    List<University> findByFacultiesContaining(String area);
}