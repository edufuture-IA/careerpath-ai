package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.model.University;
import java.util.List;
import java.util.Optional;

public interface UniversityService {
    List<University> findAll();
    Optional<University> findById(Long id);
    University save(University university);
    University update(Long id, University university);
    void delete(Long id);
    List<University> findAllById(List<Long> ids);
    
    // NUEVO MÉTODO
    List<University> findByFacultiesContaining(String faculties);

    // US20: la universidad edita solo los datos de su propia institucion
    University updateOwnInfo(Long id, String website, String city, String logoUrl,
                             String description, String faculties);

    // US21: estadisticas agrupadas de afinidad por carrera
    pe.edu.upc.careerpath_ai.util.AffinityStats.Result affinityStats(University university);
}