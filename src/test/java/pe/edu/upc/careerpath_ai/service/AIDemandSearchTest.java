package pe.edu.upc.careerpath_ai.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import pe.edu.upc.careerpath_ai.model.Career;
import pe.edu.upc.careerpath_ai.repository.TestResultRepository;
import pe.edu.upc.careerpath_ai.service.impl.AIServiceImpl;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AIDemandSearchTest {

    private static final String SEARCH_URL = "https://search.test/web_search";
    private static final String CHAT_URL = "http://chat.test/v1/chat/completions";
    private static final Career CAREER = Career.builder().name("Veterinaria").area("Salud").build();

    private final RestTemplate rest = mock(RestTemplate.class);
    private final AIServiceImpl service = new AIServiceImpl(rest, mock(TestResultRepository.class));

    private AIServiceImpl configured(String searchKey, String chatAnswer) {
        ReflectionTestUtils.setField(service, "apiKey", "k");
        ReflectionTestUtils.setField(service, "apiUrl", CHAT_URL);
        ReflectionTestUtils.setField(service, "modelName", "m");
        ReflectionTestUtils.setField(service, "searchUrl", SEARCH_URL);
        ReflectionTestUtils.setField(service, "searchApiKey", searchKey);
        when(rest.postForEntity(eq(CHAT_URL), any(), eq(Map.class))).thenReturn(ResponseEntity.ok(
                Map.of("choices", List.of(Map.of("message", Map.of("content", chatAnswer))))));
        return service;
    }

    private void searchReturns(List<Map<String, String>> results) {
        when(rest.postForEntity(eq(SEARCH_URL), any(), eq(Map.class))).thenReturn(ResponseEntity.ok(Map.of("results", results)));
    }

    @Test
    void conDatosDeInternetCalculaElPromedioDeLosCriterios() {
        searchReturns(List.of(Map.of("title", "Informe laboral", "content", "faltan veterinarios")));

        Double demand = configured("clave", "8, 7, 6, 5").estimateDemandIndex(CAREER);

        assertEquals(0.65, demand);
        ArgumentCaptor<HttpEntity> chat = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).postForEntity(eq(CHAT_URL), chat.capture(), eq(Map.class));
        Map<?, ?> body = (Map<?, ?>) chat.getValue().getBody();
        assertTrue(body.toString().contains("faltan veterinarios"));
        assertEquals(0.0, body.get("temperature"));
    }

    @Test
    void sinClaveNoInventaUnNumero() {
        assertNull(configured("", "8,7,6,5").estimateDemandIndex(CAREER));

        verify(rest, never()).postForEntity(eq(SEARCH_URL), any(), eq(Map.class));
        verify(rest, never()).postForEntity(eq(CHAT_URL), any(), eq(Map.class));
    }

    @Test
    void sinResultadosDeBusquedaNoEstima() {
        searchReturns(List.of());

        assertNull(configured("clave", "8,7,6,5").estimateDemandIndex(CAREER));
        verify(rest, never()).postForEntity(eq(CHAT_URL), any(), eq(Map.class));
    }

    @Test
    void siLaBusquedaFallaNoEstima() {
        when(rest.postForEntity(eq(SEARCH_URL), any(), eq(Map.class))).thenThrow(new RuntimeException("sin red"));

        assertNull(configured("clave", "8,7,6,5").estimateDemandIndex(CAREER));
    }

    @Test
    void respuestasInvalidasDelModeloNoSeGuardan() {
        searchReturns(List.of(Map.of("title", "t", "content", "c")));

        assertNull(configured("clave", "no se").estimateDemandIndex(CAREER));
        assertNull(configured("clave", "11, 5, 5, 5").estimateDemandIndex(CAREER));
        assertNull(configured("clave", "8, 7").estimateDemandIndex(CAREER));
    }
}
