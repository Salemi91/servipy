package py.com.servipy.client.application.dto;

import java.time.Instant;
import java.time.LocalDate;

public record ServiceRequestDetailResponse(
    Long id,
    String professionalName,
    String subject,
    String description,
    String desiredDate,
    String status,
    Instant createdAt,
    Instant updatedAt
) {}
