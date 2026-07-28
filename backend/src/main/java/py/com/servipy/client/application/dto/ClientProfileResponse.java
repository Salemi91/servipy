package py.com.servipy.client.application.dto;

import java.time.Instant;

/**
 * DTO de respuesta para el perfil del cliente.
 * No expone campos sensibles como passwordHash.
 */
public record ClientProfileResponse(
    Long id,
    String name,
    String email,
    String phone,
    String photoUrl,
    Instant updatedAt
) {}
