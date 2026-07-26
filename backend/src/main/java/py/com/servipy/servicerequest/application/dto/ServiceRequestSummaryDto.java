package py.com.servipy.servicerequest.application.dto;

import java.time.Instant;

public record ServiceRequestSummaryDto(
    Long id,
    String clientName,
    String subject,
    String status,
    Instant createdAt
) {}
