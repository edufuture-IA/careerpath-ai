package pe.edu.upc.careerpath_ai.dto;
import lombok.Data;

@Data
public class AIChatRequest {
    private Long testResultId; // Para el análisis automático
    private String prompt;     // Para el chat
}