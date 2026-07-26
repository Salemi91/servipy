package py.com.servipy.professional.application.dto;

import java.util.List;

public record ProfessionalDetailDto(
        Long id,
        String name,
        String photoUrl,
        String phone,
        String whatsapp,
        String description,
        String cityName,
        String availability,
        List<OfferedServiceDto> services
) {
}
