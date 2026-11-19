package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.careerpath_ai.model.University;
import pe.edu.upc.careerpath_ai.repository.UniversityRepository;
import pe.edu.upc.careerpath_ai.service.UniversityService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UniversityServiceImpl implements UniversityService {

    private final UniversityRepository universityRepository;

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
}