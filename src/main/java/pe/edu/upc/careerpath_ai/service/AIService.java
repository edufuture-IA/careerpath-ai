package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.model.University;
import java.util.List;

public interface AIService {
    String generateAnalysis(TestResult result);
    String chatWithAI(TestResult result, String userMessage);
    Double estimateDemandIndex(Career career);
    String generateDayInTheLife(Career career);
    String explainSimply(Career career);
    String generateRoadmap(TestResult result);
    String compareCareers(List<Career> careers);
    
    // 🆕 Método para generar el "Acerca de" de la universidad
    String generateUniversityInfo(University university);
}