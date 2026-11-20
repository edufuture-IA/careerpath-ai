package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.careerpath_ai.model.University;
import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.repository.TestResultRepository;
import pe.edu.upc.careerpath_ai.repository.UniversityRepository;
import pe.edu.upc.careerpath_ai.util.AffinityStats;
import pe.edu.upc.careerpath_ai.service.UniversityService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UniversityServiceImpl implements UniversityService {

    private final UniversityRepository universityRepository;
    private final TestResultRepository testResultRepository;

    @Override
    public List<University> findAll() { return universityRepository.findAll(); }

    @Override
    public Optional<University> findById(Long id) { return universityRepository.findById(id); }

    @Override
    public University save(University university) { return universityRepository.save(university); }

    @Override
    public University update(Long id, University university) {
        University existing = universityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Universidad no encontrada"));
        existing.setName(university.getName());
        // Agrega aquí los demás campos de tu universidad (direccion, telefono, etc.)
        return universityRepository.save(existing);
    }

    @Override
    public void delete(Long id) { universityRepository.deleteById(id); }

    @Override
    public List<University> findAllById(List<Long> ids) {
        return universityRepository.findAllById(ids);
    }

    // NUEVO MÉTODO IMPLEMENTADO
    @Override
    public List<University> findByFacultiesContaining(String faculties) {
        return universityRepository.findByFacultiesContaining(faculties);
    }

    @Override
    public University updateOwnInfo(Long id, String website, String city, String logoUrl,
                                    String description, String faculties) {
        University existing = universityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Universidad no encontrada"));
        existing.setWebsite(website);
        existing.setCity(city);
        existing.setLogoUrl(logoUrl);
        existing.setDescription(description);
        existing.setFaculties(faculties);
        return universityRepository.save(existing);
    }

    @Override
    public AffinityStats.Result affinityStats(University university) {
        List<String> careerNames = university.getCareers().stream().map(Career::getName).toList();
        List<String> recommended = testResultRepository.findAll().stream()
                .map(TestResult::getRecommendedCareers).toList();
        return AffinityStats.compute(careerNames, recommended);
    }
}
