package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.careerpath_ai.model.ActivityLog;
import pe.edu.upc.careerpath_ai.repository.ActivityLogRepository;
import pe.edu.upc.careerpath_ai.service.ActivityLogService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    public void log(String action, String description, String username, String role, String ip) {
        ActivityLog log = ActivityLog.builder()
                .action(action)
                .description(description)
                .username(username != null ? username : "anónimo")
                .role(role != null ? role : "—")
                .timestamp(LocalDateTime.now())
                .ipAddress(ip != null ? ip : "—")
                .build();
        activityLogRepository.save(log);
    }

    @Override
    public List<ActivityLog> findAll() {
        return activityLogRepository.findAllByOrderByTimestampDesc();
    }

    @Override
    public List<ActivityLog> latest50() {
        return activityLogRepository.findTop50ByOrderByTimestampDesc();
    }

    @Override
    public List<ActivityLog> byAction(String action) {
        return activityLogRepository.findByActionOrderByTimestampDesc(action);
    }

    @Override
    public long count() {
        return activityLogRepository.count();
    }
}