package py.com.servipy.professional.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.com.servipy.city.domain.City;
import py.com.servipy.professional.application.dto.CreateProfessionalProfileRequest;
import py.com.servipy.professional.application.dto.ProfessionalProfileResponse;
import py.com.servipy.professional.domain.ApprovalStatus;
import py.com.servipy.professional.domain.Availability;
import py.com.servipy.professional.domain.ProfessionalProfile;
import py.com.servipy.professional.infrastructure.persistence.ProfessionalProfileRepository;
import py.com.servipy.shared.exception.DuplicateEmailException;
import py.com.servipy.shared.exception.ResourceNotFoundException;
import py.com.servipy.user.domain.User;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProfessionalProfileService {

    private final ProfessionalProfileRepository profileRepository;
    private final EntityManager entityManager;

    public ProfessionalProfileService(ProfessionalProfileRepository profileRepository,
                                      EntityManager entityManager) {
        this.profileRepository = profileRepository;
        this.entityManager = entityManager;
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
        // Verificar que no tenga perfil ya
        if (profileRepository.findByUserId(userId).isPresent()) {
            throw new DuplicateEmailException("Ya existe un perfil profesional para este usuario");
        }

        // Validar availability
        Availability availability;
        try {
            availability = Availability.valueOf(request.availability().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Disponibilidad inválida. Valores permitidos: PRESENCIAL, VIRTUAL, AMBOS");
        }

        // Obtener referencias
        User user = entityManager.getReference(User.class, userId);
        City city = entityManager.find(City.class, request.cityId());
        if (city == null) {
            throw new ResourceNotFoundException("Ciudad no encontrada con id: " + request.cityId());
        }

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
