package pe.edu.upc.careerpath_ai.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.model.TestResult;
import pe.edu.upc.careerpath_ai.model.University;
import pe.edu.upc.careerpath_ai.repository.TestResultRepository;
import pe.edu.upc.careerpath_ai.service.AIService;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements AIService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:http://localhost:11434/v1/chat/completions}")
    private String apiUrl;

    @Value("${gemini.api.model:llama3.2}")
    private String modelName;

    private final RestTemplate restTemplate;
    private final TestResultRepository testResultRepository;

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+(?:[.,]\\d+)?)");

    @Override
    public String generateAnalysis(TestResult result) {
        if (result.getAiAnalysis() != null && !result.getAiAnalysis().trim().isEmpty()) {
            return result.getAiAnalysis();
        }
        String prompt = "Actúa como un orientador vocacional experto. El estudiante " +
                result.getStudent().getFullName() +
                " ha completado un test. Su área predominante es " + result.getTopArea() +
                " con los siguientes puntajes: " + result.getAreaScores() +
                ". Las carreras recomendadas son: " + result.getRecommendedCareers() +
                ". Escribe un párrafo motivador y profesional (máximo 120 palabras) explicando por qué estas carreras encajan con su perfil.";
        
        String response = callOllamaAPI(prompt);
        
        if (response != null && !response.startsWith("⚠️") && !response.startsWith("Error") && !response.startsWith("Lo siento")) {
            result.setAiAnalysis(response);
            testResultRepository.save(result);
        }
        return response;
    }

    @Override
    public String chatWithAI(TestResult result, String userMessage) {
        String systemPrompt = "Contexto: El alumno " + result.getStudent().getFullName() +
                " obtuvo como área principal " + result.getTopArea() +
                " y se le recomendaron las carreras: " + result.getRecommendedCareers() +
                ". Responde de forma amable, breve y útil a la siguiente pregunta del alumno: ";
        return callOllamaAPI(systemPrompt + userMessage);
    }

    @Override
    public Double estimateDemandIndex(Career career) {
        String prompt = """
                Eres un analista de mercado laboral peruano. Estima el índice de demanda
                de la carrera "%s" (área: %s) en Perú para el año en curso.
                Considera empleabilidad, ofertas en portales laborales y crecimiento sectorial.
                Devuelve SOLO un número decimal entre 0.00 y 1.00 (por ejemplo: 0.82).
                No agregues texto, ni comillas, ni explicación.
                """.formatted(career.getName(),
                career.getArea() == null ? "General" : career.getArea());
        String raw = callOllamaAPI(prompt);
        return extractDouble(raw);
    }

    @Override
    public String generateDayInTheLife(Career career) {
        String prompt = "Describe un día típico en la vida de un profesional de " + career.getName() + 
                " en Perú. Menciónalo en formato de línea de tiempo (ej. 8:00 AM, 10:00 AM, etc.). " +
                "Usa un tono inspirador, realista y atractivo para un joven de 17 años. Máximo 150 palabras.";
        return callOllamaAPI(prompt);
    }

    @Override
    public String explainSimply(Career career) {
        String prompt = "Explícame qué hace un profesional de " + career.getName() + 
                " como si tuviera 15 años, usando analogías divertidas y un lenguaje muy sencillo. " +
                "Evita tecnicismos. Máximo 80 palabras.";
        return callOllamaAPI(prompt);
    }

    @Override
    public String generateRoadmap(TestResult result) {
        String prompt = "El estudiante " + result.getStudent().getFullName() + 
                " obtuvo un perfil con área predominante en " + result.getTopArea() + 
                " y le recomendamos las carreras: " + result.getRecommendedCareers() + ". " +
                "Actúa como un orientador vocacional. Arma una ruta de aprendizaje de 3 pasos (Paso 1, Paso 2, Paso 3). " +
                "Dile qué debe aprender primero, qué curso tomar y qué habilidad blanda desarrollar. Sé muy práctico y usa viñetas.";
        return callOllamaAPI(prompt);
    }

    @Override
    public String compareCareers(List<Career> careers) {
        if (careers == null || careers.size() < 2) {
            return "⚠️ Se necesitan al menos 2 carreras para comparar.";
        }

        StringBuilder nombres = new StringBuilder();
        for (int i = 0; i < careers.size(); i++) {
            nombres.append(careers.get(i).getName());
            if (i < careers.size() - 1) nombres.append(", ");
        }

        String prompt = "Compara las siguientes carreras en Perú: " + nombres.toString() + ".\n\n" +
                "Por favor, responde usando EXACTAMENTE este formato:\n" +
                "**1. Futuro a 5 años:**\n[Tu análisis aquí]\n\n" +
                "**2. Dificultad de estudio:**\n[Tu análisis aquí]\n\n" +
                "**3. Consejo final:**\n[Tu consejo aquí]\n\n" +
                "Sé directo, objetivo y no uses más de 150 palabras en total.";
                
        return callOllamaAPI(prompt);
    }

    // 🆕 NUEVO MÉTODO PARA UNIVERSIDADES
    @Override
    public String generateUniversityInfo(University uni) {
        String prompt = "Actúa como un orientador vocacional experto. Escribe un párrafo atractivo (máximo 100 palabras) " +
                "para convencer a un estudiante de secundaria de estudiar en la universidad " + uni.getName() + 
                " ubicada en " + (uni.getCity() != null ? uni.getCity() : "Perú") + ". " +
                "Destaca sus facultades: " + (uni.getFaculties() != null ? uni.getFaculties() : "diversas áreas") + ". " +
                "Usa un tono inspirador y menciona por qué es una buena opción para su futuro profesional.";
        return callOllamaAPI(prompt);
    }

    private Double extractDouble(String raw) {
        if (raw == null || raw.startsWith("⚠️") || raw.startsWith("Error") || raw.startsWith("No se pudo")) return null;
        Matcher m = NUMBER_PATTERN.matcher(raw);
        if (!m.find()) return null;
        try {
            double v = Double.parseDouble(m.group(1).replace(",", "."));
            if (v > 1.0 && v <= 100.0) v = v / 100.0;
            return Math.max(0.0, Math.min(1.0, v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String callOllamaAPI(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", modelName,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("Cuota de Ollama excedida (Error 429)");
            return "⚠️ He alcanzado mi límite de consultas por hoy. Intenta de nuevo en unas horas.";
        } catch (HttpClientErrorException e) {
            log.error("Error HTTP de Ollama: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "⚠️ Error de API (" + e.getStatusCode() + "). Revisa tu API Key o el modelo.";
        } catch (ResourceAccessException e) {
            log.error("Error de red al conectar con Ollama: {}", e.getMessage());
            return "⚠️ Error de red. Revisa tu conexión a internet.";
        } catch (Exception e) {
            log.error("Error inesperado al llamar a Ollama", e);
            return "⚠️ Error inesperado en el servidor: " + e.getMessage();
        }
        
        return "⚠️ No se pudo generar una respuesta válida.";
    }
}