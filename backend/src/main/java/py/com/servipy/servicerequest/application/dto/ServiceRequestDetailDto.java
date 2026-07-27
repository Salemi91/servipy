package py.com.servipy.servicerequest.application.dto;

import java.time.Instant;
import java.time.LocalDate;

public record ServiceRequestDetailDto(
    Long id,
    String clientName,
    String clientEmail,
    String clientPhone,
    String subject,
    String description,
    LocalDate desiredDate,
    String status,
    Instant createdAt,
    Instant updatedAt
) {}
