package pe.edu.upc.careerpath_ai.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import pe.edu.upc.careerpath_ai.dto.ErrorResponse;

import java.io.IOException;

/**
 * Respuestas JSON para 401 (token ausente o inválido) y 403 (rol insuficiente) en la API.
 * Usa el mismo formato que {@link pe.edu.upc.careerpath_ai.controller.api.ApiExceptionHandler}.
 */
public final class RestSecurityHandlers {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private RestSecurityHandlers() {}

    public static AuthenticationEntryPoint entryPoint() {
        return (req, res, ex) -> write(req, res, 401, "Unauthorized",
                "Token ausente, inválido o expirado");
    }

    public static AccessDeniedHandler accessDeniedHandler() {
        return (req, res, ex) -> write(req, res, 403, "Forbidden",
                "No tienes permisos para acceder a este recurso");
    }

    private static void write(HttpServletRequest req, HttpServletResponse res, int status,
                              String error, String message) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");

        ErrorResponse body = ErrorResponse.of(status, error, message, req.getRequestURI());
        MAPPER.writeValue(res.getWriter(), body);
    }
}