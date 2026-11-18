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

    private final RestTemplate rest = mock(RestTemplate.class);
    private final AIServiceImpl service = new AIServiceImpl(rest, mock(TestResultRepository.class));

    private AIServiceImpl configured(String searchKey) {
        ReflectionTestUtils.setField(service, "apiKey", "k");
        ReflectionTestUtils.setField(service, "apiUrl", CHAT_URL);
        ReflectionTestUtils.setField(service, "modelName", "m");
        ReflectionTestUtils.setField(service, "searchUrl", SEARCH_URL);
        ReflectionTestUtils.setField(service, "searchApiKey", searchKey);
        when(rest.postForEntity(eq(CHAT_URL), any(), eq(Map.class))).thenReturn(ResponseEntity.ok(
                Map.of("choices", List.of(Map.of("message", Map.of("content", "0.82"))))));
        return service;
    }

    @Test
    void conClaveBuscaEnInternetYMetelosResultadosEnElPrompt() {
        when(rest.postForEntity(eq(SEARCH_URL), any(), eq(Map.class))).thenReturn(ResponseEntity.ok(
                Map.of("results", List.of(Map.of("title", "Informe laboral", "content", "alta demanda de abogados")))));

        Double demand = configured("clave-busqueda").estimateDemandIndex(Career.builder().name("Derecho").area("Legal").build());

        assertEquals(0.82, demand);
        ArgumentCaptor<HttpEntity> chat = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).postForEntity(eq(CHAT_URL), chat.capture(), eq(Map.class));
        assertTrue(chat.getValue().getBody().toString().contains("alta demanda de abogados"));
    }

    @Test
    void sinClaveNoBuscaPeroIgualEstima() {
        Double demand = configured("").estimateDemandIndex(Career.builder().name("Derecho").build());

        assertEquals(0.82, demand);
        verify(rest, never()).postForEntity(eq(SEARCH_URL), any(), eq(Map.class));
    }

    @Test
    void siLaBusquedaFallaIgualEstima() {
        when(rest.postForEntity(eq(SEARCH_URL), any(), eq(Map.class))).thenThrow(new RuntimeException("sin red"));

        assertEquals(0.82, configured("clave-busqueda").estimateDemandIndex(Career.builder().name("Derecho").build()));
    }
}
