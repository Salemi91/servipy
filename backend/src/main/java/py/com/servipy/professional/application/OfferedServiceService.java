package py.com.servipy.professional.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.servipy.category.application.CategoryService;
import py.com.servipy.category.domain.Category;
import py.com.servipy.professional.application.dto.CreateOfferedServiceRequest;
import py.com.servipy.professional.application.dto.OfferedServiceDto;
import py.com.servipy.professional.domain.OfferedService;
import py.com.servipy.professional.domain.ProfessionalProfile;
import py.com.servipy.professional.infrastructure.persistence.OfferedServiceRepository;
import py.com.servipy.professional.infrastructure.persistence.ProfessionalProfileRepository;
import py.com.servipy.shared.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Gestión del tarifario del profesional autenticado.
 * Un profesional solo ve y modifica los servicios de su propio perfil.
 */
@Service
@Transactional(readOnly = true)
public class OfferedServiceService {

    private final OfferedServiceRepository offeredServiceRepository;
    private final ProfessionalProfileRepository profileRepository;
    private final CategoryService categoryService;

    public OfferedServiceService(OfferedServiceRepository offeredServiceRepository,
                                 ProfessionalProfileRepository profileRepository,
                                 CategoryService categoryService) {
        this.offeredServiceRepository = offeredServiceRepository;
        this.profileRepository = profileRepository;
        this.categoryService = categoryService;
    }

    public List<OfferedServiceDto> findOwnServices(Long userId) {
        ProfessionalProfile profile = findOwnProfile(userId);
        return offeredServiceRepository.findByProfessionalIdAndActiveTrueOrderByIdAsc(profile.getId()).stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public OfferedServiceDto create(Long userId, CreateOfferedServiceRequest request) {
        ProfessionalProfile profile = findOwnProfile(userId);
        Category category = categoryService.findActiveEntityById(request.categoryId());

        OfferedService offeredService = new OfferedService();
        offeredService.setProfessional(profile);
        offeredService.setCategory(category);
        offeredService.setName(request.name().trim());
        offeredService.setDescription(request.description() != null ? request.description().trim() : null);
        offeredService.setPrice(request.price());
        offeredService.setCurrency(request.currencyOrDefault());
        offeredService.setActive(true);

        return toDto(offeredServiceRepository.save(offeredService));
    }

    /**
     * Baja lógica: el servicio deja de publicarse pero se conserva
     * para no alterar las solicitudes ya recibidas.
     */
    @Transactional
    public void deactivate(Long userId, Long serviceId) {
        ProfessionalProfile profile = findOwnProfile(userId);

        OfferedService offeredService = offeredServiceRepository
            .findByIdAndProfessionalIdAndActiveTrue(serviceId, profile.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        offeredService.setActive(false);
        offeredServiceRepository.save(offeredService);
    }

    private ProfessionalProfile findOwnProfile(Long userId) {
        return profileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Perfil profesional no encontrado"));
    }

    private OfferedServiceDto toDto(OfferedService s) {
        return new OfferedServiceDto(
            s.getId(),
            s.getName(),
            s.getDescription(),
            s.getPrice(),
            s.getCurrency(),
            s.getCategory().getName()
        );
    }
}
