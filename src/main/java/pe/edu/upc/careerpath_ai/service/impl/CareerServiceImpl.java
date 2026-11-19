package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.careerpath_ai.exception.RecursoNoEncontradoException;
import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.repository.CareerRepository;
import pe.edu.upc.careerpath_ai.service.AIService;
import pe.edu.upc.careerpath_ai.service.CareerService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CareerServiceImpl implements CareerService {

    private final CareerRepository careerRepository;
    private final AIService aiService;

    private static final int DEMAND_COOLDOWN_HOURS = 24;
    private static final int DEMAND_STALE_DAYS   = 7;

    @Override
    public List<Career> findAll() { return careerRepository.findAll(); }

    @Override
    public Optional<Career> findById(Long id) { return careerRepository.findById(id); }

    @Override
    public Career save(Career career) {
        if (career.getId() != null) {
            return careerRepository.save(career);
        }
        career.setDemandIndex(0.0);
        career.setDemandIndexUpdatedAt(null);
        career.setDemandIndexSource("PENDING");
        Career saved = careerRepository.save(career);
        return refreshDemandIndex(saved.getId(), true);
    }

    @Override
    public Career update(Long id, Career career) {
        Career existing = careerRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrera no encontrada"));
        existing.setName(career.getName());
        existing.setDescription(career.getDescription());
        existing.setArea(career.getArea());
        existing.setRequiredSkills(career.getRequiredSkills());
        return careerRepository.save(existing);
    }

    @Override
    public void delete(Long id) { careerRepository.deleteById(id); }

    @Override
    public List<Career> findAllById(List<Long> ids) {
        return careerRepository.findAllById(ids);
    }

    // =====================================================
    // 🆕 DEMANDA CON IA (SIN @Transactional: la llamada a la IA es lenta)
    // =====================================================
    @Override
    public Career findByIdWithFreshDemand(Long id, int maxAgeDays) {
        Career career = careerRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrera no encontrada"));

        boolean stale = career.getDemandIndexUpdatedAt() == null
                || career.getDemandIndexUpdatedAt()
                    .isBefore(LocalDateTime.now().minusDays(maxAgeDays));

        if (stale) {
            return refreshDemandIndex(id, false);
        }
        return career;
    }

    @Override
    public Career refreshDemandIndex(Long careerId, boolean force) {
        Career career = careerRepository.findById(careerId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrera no encontrada"));

        // Cooldown: no llamar a la IA si se actualizó hace < 24h (salvo force)
        if (!force && career.getDemandIndexUpdatedAt() != null
                && career.getDemandIndexUpdatedAt()
                    .isAfter(LocalDateTime.now().minusHours(DEMAND_COOLDOWN_HOURS))) {
            return career;
        }
        // Si el índice fue ajustado manualmente, no sobreescribir
        if (!force && "MANUAL".equalsIgnoreCase(career.getDemandIndexSource())) {
            return career;
        }

        Double aiIndex = aiService.estimateDemandIndex(career);
        if (aiIndex == null) {
            log.warn("La IA no devolvió un índice válido para {}", career.getName());
            return career;
        }

        career.setDemandIndex(aiIndex);
        career.setDemandIndexUpdatedAt(LocalDateTime.now());
        career.setDemandIndexSource("AI_GEMINI");
        log.info("Índice IA actualizado para {}: {}", career.getName(), aiIndex);
        return careerRepository.save(career);
    }

    @Override
    @Scheduled(cron = "0 0 3 * * MON")   // lunes 3 AM
    public void refreshAllDemandIndices() {
        log.info("Iniciando refresh programado de índices de demanda");
        List<Career> careers = careerRepository.findAll();
        int updated = 0;
        for (Career c : careers) {
            boolean stale = c.getDemandIndexUpdatedAt() == null
                    || c.getDemandIndexUpdatedAt()
                        .isBefore(LocalDateTime.now().minusDays(DEMAND_STALE_DAYS));
            if (stale) {
                try {
                    refreshDemandIndex(c.getId(), false);
                    updated++;
                } catch (Exception e) {
                    log.error("Fallo al refrescar demanda de {}", c.getName(), e);
                }
            }
        }
        log.info("Refresh completado. Carreras actualizadas: {}", updated);
    }
}