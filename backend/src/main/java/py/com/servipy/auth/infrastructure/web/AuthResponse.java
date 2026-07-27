package py.com.servipy.auth.infrastructure.web;

/**
 * DTO de respuesta para login y registro exitoso.
 */
public record AuthResponse(
    String accessToken,
    String tokenType,
    UserResponse user
) {

    /**
     * Factory que fija tokenType como "Bearer".
     */
    public static AuthResponse bearer(String accessToken, UserResponse user) {
        return new AuthResponse(accessToken, "Bearer", user);
    }
}
