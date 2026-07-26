package py.com.servipy.professional.application.dto;

import java.math.BigDecimal;

public record ProfessionalSummaryDto(
        Long id,
        String name,
        String professionalTitle,
        String categoryName,
        String description,
        String cityName,
        BigDecimal referencePrice,
        String availability,
        String photoUrl
) {
}
