package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.careerpath_ai.exception.RecursoNoEncontradoException;
import pe.edu.upc.careerpath_ai.model.University;
import pe.edu.upc.careerpath_ai.repository.UniversityRepository;
import pe.edu.upc.careerpath_ai.service.UniversityService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UniversityServiceImpl implements UniversityService {

    private final UniversityRepository universityRepository;

    @Override public List<University> findAll() { return universityRepository.findAll(); }
    @Override public Optional<University> findById(Long id) { return universityRepository.findById(id); }
    @Override public University save(University u) { return universityRepository.save(u); }

    @Override
    public University update(Long id, University u) {
        University existing = universityRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Universidad no encontrada"));
        existing.setName(u.getName());
        existing.setWebsite(u.getWebsite());
        existing.setCity(u.getCity());
        existing.setLogoUrl(u.getLogoUrl());
        existing.setDescription(u.getDescription());
        existing.setFaculties(u.getFaculties());
        return universityRepository.save(existing);
    }
    @Override
    public List<University> findByFacultiesContaining(String area) {
        if (area == null || area.isBlank()) return List.of();
        return universityRepository.findAll().stream()
                .filter(u -> u.getFaculties() != null
                        && u.getFaculties().toLowerCase().contains(area.toLowerCase()))
                .toList();
    }
    @Override public void delete(Long id) { universityRepository.deleteById(id); }
}