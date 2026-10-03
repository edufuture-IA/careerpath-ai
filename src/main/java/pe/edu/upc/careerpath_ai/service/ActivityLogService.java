package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.model.ActivityLog;
import java.util.List;

public interface ActivityLogService {
    void log(String action, String description, String username, String role, String ip);
    List<ActivityLog> findAll();
    List<ActivityLog> latest50();
    List<ActivityLog> byAction(String action);
    long count();
}