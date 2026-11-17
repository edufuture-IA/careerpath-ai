package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.model.Career;
import java.util.List;
import java.util.Optional;

public interface CareerService {
    List<Career> findAll();
    Optional<Career> findById(Long id);
    Career save(Career career);
    Career update(Long id, Career career);
    void delete(Long id);
    List<Career> findAllById(List<Long> ids);

    // 🆕 Demanda IA
    Career findByIdWithFreshDemand(Long id, int maxAgeDays);
    Career refreshDemandIndex(Long careerId, boolean force);
    void refreshAllDemandIndices();
}