package py.com.servipy.professional.application.dto;

import java.time.Instant;

public record ProfessionalAdminDto(
    Long id,
    String name,
    String email,
    String phone,
    String description,
    String approvalStatus,
    Instant createdAt
) {}
