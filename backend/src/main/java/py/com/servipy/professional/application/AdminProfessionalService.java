package py.com.servipy.professional.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.com.servipy.professional.application.dto.ProfessionalAdminDto;
import py.com.servipy.professional.domain.ApprovalStatus;
import py.com.servipy.professional.domain.ProfessionalProfile;
import py.com.servipy.professional.infrastructure.persistence.ProfessionalProfileRepository;
import py.com.servipy.shared.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminProfessionalService {

    private final ProfessionalProfileRepository profileRepository;

    public AdminProfessionalService(ProfessionalProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public List<ProfessionalAdminDto> findPending() {
        return profileRepository.findByApprovalStatus(ApprovalStatus.PENDING).stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public ProfessionalAdminDto approve(Long id) {
        ProfessionalProfile profile = profileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado con id: " + id));
        profile.setApprovalStatus(ApprovalStatus.APPROVED);
        profile.setUpdatedAt(Instant.now());
        ProfessionalProfile saved = profileRepository.save(profile);
        return toDto(saved);
    }

    @Transactional
    public ProfessionalAdminDto reject(Long id) {
        ProfessionalProfile profile = profileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado con id: " + id));
        profile.setApprovalStatus(ApprovalStatus.REJECTED);
        profile.setUpdatedAt(Instant.now());
        ProfessionalProfile saved = profileRepository.save(profile);
        return toDto(saved);
    }

    private ProfessionalAdminDto toDto(ProfessionalProfile p) {
        return new ProfessionalAdminDto(
            p.getId(),
            p.getUser().getName(),
            p.getUser().getEmail(),
            p.getPhone(),
            p.getDescription(),
            p.getApprovalStatus().name(),
            p.getCreatedAt()
        );
    }
}
