package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.model.TestResult;
import java.util.List;

public interface AIService {
    String generateAnalysis(TestResult result);
    String chatWithAI(TestResult result, String userMessage);
    Double estimateDemandIndex(Career career);
    String generateDayInTheLife(Career career);
    String explainSimply(Career career);
    String generateRoadmap(TestResult result);
    
    // 🆕 Ahora recibe una lista de carreras
    String compareCareers(List<Career> careers);
}