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
import pe.edu.upc.careerpath_ai.repository.TestResultRepository;
import pe.edu.upc.careerpath_ai.service.AIService;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Value("${ollama.search.url:https://ollama.com/api/web_search}")
    private String searchUrl;

    @Value("${ollama.search.api-key:}")
    private String searchApiKey;

    private final RestTemplate restTemplate;
    private final TestResultRepository testResultRepository;

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d{1,2}\\b");

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
        String found = searchWeb("demanda laboral empleabilidad " + career.getName() + " Perú");
        if (found.isBlank()) {
            log.warn("Sin datos de internet para {}: no se estima la demanda", career.getName());
            return null;
        }
        String prompt = """
                Eres un analista del mercado laboral peruano. Usando SOLO la información de internet de abajo, califica la carrera "%s" (área: %s) en Perú con cuatro números enteros del 1 al 10:
                1) empleabilidad: 1 = casi no hay trabajo, 5 = hay trabajo regular, 10 = sobran ofertas.
                2) salario: 1 = sueldos muy bajos, 5 = sueldos medios, 10 = sueldos muy altos.
                3) crecimiento: 1 = el sector se reduce, 5 = estable, 10 = crece muy rápido en los próximos 5 años.
                4) poca competencia: 1 = hay demasiados profesionales para pocos puestos, 10 = faltan profesionales.
                Si la información no dice nada sobre un criterio, usa 5.
                Información de internet:
                %s
                Responde SOLO los cuatro números separados por comas, en ese orden, sin texto.
                """.formatted(career.getName(),
                career.getArea() == null ? "General" : career.getArea(), found);
        return scoresToIndex(callOllamaAPI(prompt, 0.0));
    }

    private String searchWeb(String query) {
        if (searchApiKey == null || searchApiKey.isBlank()) {
            log.warn("OLLAMA_API_KEY no configurada: no se puede buscar en internet");
            return "";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(searchApiKey);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(searchUrl,
                    new HttpEntity<>(Map.of("query", query, "max_results", 5), headers), Map.class);
            Object results = response.getBody() == null ? null : response.getBody().get("results");
            if (!(results instanceof List<?> list)) return "";
            StringBuilder sb = new StringBuilder();
            for (Object item : list) {
                if (item instanceof Map<?, ?> m) {
                    String content = String.valueOf(m.get("content"));
                    sb.append("- ").append(m.get("title")).append(": ")
                            .append(content.length() > 500 ? content.substring(0, 500) : content).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("Falló la búsqueda web para '{}': {}", query, e.getMessage());
            return "";
        }
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

    // 🆕 Implementación corregida para múltiples carreras
    @Override
    public String compareCareers(List<Career> careers) {
        if (careers == null || careers.size() < 2) {
            return "⚠️ Se necesitan al menos 2 carreras para comparar.";
        }

        // Construimos una lista de nombres: "Carrera A, Carrera B, Carrera C"
        StringBuilder nombres = new StringBuilder();
        for (int i = 0; i < careers.size(); i++) {
            nombres.append(careers.get(i).getName());
            if (i < careers.size() - 1) nombres.append(", ");
        }

        String prompt = "Compara las siguientes carreras en Perú: " + nombres.toString() + ". " +
                "Dime en 3 puntos clave: 1) Cuál tiene mejor futuro a 5 años, 2) Cuál es más difícil de estudiar, " +
                "3) Un consejo para elegir entre ellas. Sé directo, objetivo y usa viñetas. Máximo 150 palabras.";
        return callOllamaAPI(prompt);
    }

    private Double scoresToIndex(String raw) {
        if (raw == null || raw.startsWith("⚠️")) return null;
        List<Integer> scores = new ArrayList<>();
        Matcher m = NUMBER_PATTERN.matcher(raw);
        while (m.find() && scores.size() < 4) scores.add(Integer.parseInt(m.group()));
        if (scores.size() < 4 || scores.stream().anyMatch(v -> v < 1 || v > 10)) return null;
        return Math.round(scores.stream().mapToInt(Integer::intValue).sum() / 40.0 * 100) / 100.0;
    }

    private String callOllamaAPI(String prompt) {
        return callOllamaAPI(prompt, null);
    }

    @SuppressWarnings("unchecked")
    private String callOllamaAPI(String prompt, Double temperature) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", modelName);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        if (temperature != null) body.put("temperature", temperature);

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