package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.careerpath_ai.exception.RecursoNoEncontradoException;
import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.repository.CareerRepository;
import pe.edu.upc.careerpath_ai.service.CareerService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CareerServiceImpl implements CareerService {

    private final CareerRepository careerRepository;

    @Override
    public List<Career> findAll() { return careerRepository.findAll(); }

    @Override
    public Optional<Career> findById(Long id) { return careerRepository.findById(id); }

    @Override
    public Career save(Career career) { return careerRepository.save(career); }

    @Override
    public Career update(Long id, Career career) {
        Career existing = careerRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrera no encontrada"));
        existing.setName(career.getName());
        existing.setDescription(career.getDescription());
        existing.setArea(career.getArea());
        existing.setRequiredSkills(career.getRequiredSkills());
        existing.setDemandIndex(career.getDemandIndex());
        return careerRepository.save(existing);
    }

    @Override
    public void delete(Long id) { careerRepository.deleteById(id); }
}