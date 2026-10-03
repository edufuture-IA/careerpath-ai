package pe.edu.upc.careerpath_ai.dto;

public record AuthResponse(String token, String tokenType, long expiresInMs, UserDTO user) {
    public static AuthResponse bearer(String token, long expiresInMs, UserDTO user) {
        return new AuthResponse(token, "Bearer", expiresInMs, user);
    }
}
