package pe.edu.upc.careerpath_ai.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;

/** Respuestas JSON para 401 (token ausente o inválido) y 403 (rol insuficiente) en la API. */
public final class RestSecurityHandlers {

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
        String body = "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}"
                .formatted(LocalDateTime.now(), status, error, message,
                        req.getRequestURI().replace("\"", ""));
        res.getWriter().write(body);
    }
}
