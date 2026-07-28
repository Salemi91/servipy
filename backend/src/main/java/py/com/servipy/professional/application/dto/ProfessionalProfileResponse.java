package py.com.servipy.professional.application.dto;

import java.time.Instant;

public record ProfessionalProfileResponse(
    Long id,
    String phone,
    String whatsapp,
    String description,
    Long cityId,
    String cityName,
    String availability,
    String approvalStatus,
    Instant createdAt
) {}
