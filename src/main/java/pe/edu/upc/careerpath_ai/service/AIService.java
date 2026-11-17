package pe.edu.upc.careerpath_ai.service;

import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.model.TestResult;

public interface AIService {
    String generateAnalysis(TestResult result);
    String chatWithAI(TestResult result, String userMessage);
    Double estimateDemandIndex(Career career);

    // 🆕 NUEVOS MÉTODOS
    String generateDayInTheLife(Career career);
    String explainSimply(Career career);
    String generateRoadmap(TestResult result);
    String compareCareers(Career c1, Career c2);
}