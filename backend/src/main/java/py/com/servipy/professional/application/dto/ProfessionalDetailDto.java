package py.com.servipy.professional.application.dto;

import java.util.List;

/**
 * Detalle público de un profesional.
 * No incluye datos de contacto: se entregan al cliente cuando su solicitud está ACCEPTED.
 */
public record ProfessionalDetailDto(
        Long id,
        String name,
        String photoUrl,
        String description,
        String cityName,
        String availability,
        List<OfferedServiceDto> services
) {
}
