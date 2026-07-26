package py.com.servipy.professional.application.dto;

import java.math.BigDecimal;

public record OfferedServiceDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String currency,
        String categoryName
) {
}
