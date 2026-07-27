package py.com.servipy.client.application.dto;

import java.time.Instant;

public record ServiceRequestResponse(
    Long id,
    String serviceName,
    String professionalName,
    String status,
    Instant createdAt,
    Instant updatedAt
) {}
