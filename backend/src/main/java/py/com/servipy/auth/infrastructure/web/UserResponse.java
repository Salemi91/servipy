package py.com.servipy.auth.infrastructure.web;

import py.com.servipy.user.domain.Role;
import py.com.servipy.user.domain.User;

/**
 * DTO de respuesta con datos públicos del usuario.
 */
public record UserResponse(
    Long id,
    String name,
    String email,
    Role role
) {

    /**
     * Factory desde la entidad User.
     */
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole()
        );
    }
}
