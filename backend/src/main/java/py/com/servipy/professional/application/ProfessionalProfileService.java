package py.com.servipy.professional.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.servipy.city.application.CityService;
import py.com.servipy.city.domain.City;
import py.com.servipy.professional.application.dto.CreateProfessionalProfileRequest;
import py.com.servipy.professional.application.dto.ProfessionalProfileResponse;
import py.com.servipy.professional.application.exception.ProfileAlreadyExistsException;
import py.com.servipy.professional.domain.ApprovalStatus;
import py.com.servipy.professional.domain.Availability;
import py.com.servipy.professional.domain.ProfessionalProfile;
import py.com.servipy.professional.infrastructure.persistence.ProfessionalProfileRepository;
import py.com.servipy.shared.exception.ResourceNotFoundException;
import py.com.servipy.user.domain.User;
import py.com.servipy.user.infrastructure.persistence.UserRepository;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProfessionalProfileService {

    private final ProfessionalProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final CityService cityService;

    public ProfessionalProfileService(ProfessionalProfileRepository profileRepository,
                                      UserRepository userRepository,
                                      CityService cityService) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.cityService = cityService;
    }

    /**
     * Obtiene el perfil del profesional logueado.
     * Retorna Optional.empty() si no tiene perfil aún.
     */
    public Optional<ProfessionalProfileResponse> findByUserId(Long userId) {
        return profileRepository.findByUserId(userId).map(this::toResponse);
    }

    /**
     * Crea el perfil profesional para el usuario logueado.
     * El perfil se crea con estado PENDING (requiere aprobación del admin).
     */
    @Transactional
    public ProfessionalProfileResponse create(Long userId, CreateProfessionalProfileRequest request) {
        if (profileRepository.findByUserId(userId).isPresent()) {
            throw new ProfileAlreadyExistsException("Ya existe un perfil profesional para este usuario");
        }

        Availability availability = parseAvailability(request.availability());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        City city = cityService.findEntityById(request.cityId());

        ProfessionalProfile profile = new ProfessionalProfile();
        profile.setUser(user);
        profile.setPhone(request.phone().trim());
        profile.setWhatsapp(request.whatsapp() != null ? request.whatsapp().trim() : request.phone().trim());
        profile.setDescription(request.description().trim());
        profile.setCity(city);
        profile.setAvailability(availability);
        profile.setApprovalStatus(ApprovalStatus.PENDING);
        profile.setCreatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());

        ProfessionalProfile saved = profileRepository.save(profile);
        return toResponse(saved);
    }

    private Availability parseAvailability(String availability) {
        try {
            return Availability.valueOf(availability.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Disponibilidad inválida. Valores permitidos: PRESENCIAL, VIRTUAL, AMBOS");
        }
    }

    private ProfessionalProfileResponse toResponse(ProfessionalProfile p) {
        return new ProfessionalProfileResponse(
            p.getId(),
            p.getPhone(),
            p.getWhatsapp(),
            p.getDescription(),
            p.getCity() != null ? p.getCity().getId() : null,
            p.getCity() != null ? p.getCity().getName() : null,
            p.getAvailability().name(),
            p.getApprovalStatus().name(),
            p.getCreatedAt()
        );
    }
}
