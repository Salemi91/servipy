package py.com.servipy.professional.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.servipy.professional.application.dto.OfferedServiceDto;
import py.com.servipy.professional.application.dto.ProfessionalDetailDto;
import py.com.servipy.professional.application.dto.ProfessionalSummaryDto;
import py.com.servipy.professional.application.spec.ProfessionalSpecification;
import py.com.servipy.professional.domain.OfferedService;
import py.com.servipy.professional.domain.ProfessionalProfile;
import py.com.servipy.professional.infrastructure.persistence.ProfessionalProfileRepository;
import py.com.servipy.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProfessionalCatalogService {

    private final ProfessionalProfileRepository profileRepository;

    public ProfessionalCatalogService(ProfessionalProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /**
     * Lista profesionales activos y aprobados con filtrado dinámico.
     */
    public Page<ProfessionalSummaryDto> findAll(Long categoryId, Long cityId, String search, Pageable pageable) {
        Specification<ProfessionalProfile> spec = ProfessionalSpecification.build(categoryId, cityId, search);
        Page<ProfessionalProfile> page = profileRepository.findAll(spec, pageable);
        return page.map(this::toSummaryDto);
    }

    /**
     * Cantidad de profesionales visibles en el catálogo por categoría.
     * Las categorías sin profesionales visibles no aparecen en el mapa.
     */
    public Map<Long, Long> countByCategory() {
        return profileRepository.countActiveProfessionalsByCategory().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Detalle de un profesional activo/aprobado por id.
     */
    public ProfessionalDetailDto findById(Long id) {
        ProfessionalProfile profile = profileRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado"));
        return toDetailDto(profile);
    }

    private ProfessionalSummaryDto toSummaryDto(ProfessionalProfile p) {
        List<OfferedService> activeServices = p.getOfferedServices().stream()
                .filter(OfferedService::getActive)
                .toList();

        OfferedService first = activeServices.isEmpty() ? null : activeServices.get(0);

        return new ProfessionalSummaryDto(
                p.getId(),
                p.getUser().getName(),
                first != null ? first.getName() : null,
                first != null ? first.getCategory().getName() : null,
                truncate(p.getDescription(), 150),
                p.getCity() != null ? p.getCity().getName() : null,
                activeServices.stream()
                        .map(OfferedService::getPrice)
                        .min(BigDecimal::compareTo)
                        .orElse(null),
                p.getAvailability().name(),
                p.getPhotoUrl()
        );
    }

    private ProfessionalDetailDto toDetailDto(ProfessionalProfile p) {
        List<OfferedServiceDto> services = p.getOfferedServices().stream()
                .filter(OfferedService::getActive)
                .map(os -> new OfferedServiceDto(
                        os.getId(),
                        os.getName(),
                        os.getDescription(),
                        os.getPrice(),
                        os.getCurrency(),
                        os.getCategory().getName()
                ))
                .toList();

        return new ProfessionalDetailDto(
                p.getId(),
                p.getUser().getName(),
                p.getPhotoUrl(),
                p.getDescription(),
                p.getCity() != null ? p.getCity().getName() : null,
                p.getAvailability().name(),
                services
        );
    }

    private String truncate(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }
}
