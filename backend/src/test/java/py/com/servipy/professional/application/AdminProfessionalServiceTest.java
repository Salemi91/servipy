package py.com.servipy.professional.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import py.com.servipy.professional.application.dto.ProfessionalAdminDto;
import py.com.servipy.professional.domain.ApprovalStatus;
import py.com.servipy.professional.domain.Availability;
import py.com.servipy.professional.domain.ProfessionalProfile;
import py.com.servipy.professional.infrastructure.persistence.ProfessionalProfileRepository;
import py.com.servipy.shared.exception.ResourceNotFoundException;
import py.com.servipy.user.domain.Role;
import py.com.servipy.user.domain.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProfessionalServiceTest {

    @Mock
    private ProfessionalProfileRepository profileRepository;

    @InjectMocks
    private AdminProfessionalService adminProfessionalService;

    private ProfessionalProfile sampleProfile;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Juan Pérez", "juan@test.com", "hashedpw", Role.PROFESSIONAL);
        sampleUser.setId(10L);

        sampleProfile = new ProfessionalProfile();
        sampleProfile.setId(1L);
        sampleProfile.setUser(sampleUser);
        sampleProfile.setPhone("0981123456");
        sampleProfile.setDescription("Plomero con experiencia");
        sampleProfile.setApprovalStatus(ApprovalStatus.PENDING);
        sampleProfile.setAvailability(Availability.PRESENCIAL);
        sampleProfile.setCreatedAt(Instant.parse("2024-06-01T10:00:00Z"));
        sampleProfile.setUpdatedAt(Instant.parse("2024-06-01T10:00:00Z"));
    }

    @Test
    void should_returnPendingProfessionals_when_pendingExist() {
        // Arrange
        when(profileRepository.findByApprovalStatus(ApprovalStatus.PENDING))
            .thenReturn(List.of(sampleProfile));

        // Act
        List<ProfessionalAdminDto> result = adminProfessionalService.findPending();

        // Assert
        assertThat(result).hasSize(1);
        ProfessionalAdminDto dto = result.get(0);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Juan Pérez");
        assertThat(dto.email()).isEqualTo("juan@test.com");
        assertThat(dto.approvalStatus()).isEqualTo("PENDING");
        verify(profileRepository).findByApprovalStatus(ApprovalStatus.PENDING);
    }

    @Test
    void should_returnEmptyList_when_noPendingExist() {
        // Arrange
        when(profileRepository.findByApprovalStatus(ApprovalStatus.PENDING))
            .thenReturn(List.of());

        // Act
        List<ProfessionalAdminDto> result = adminProfessionalService.findPending();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void should_approveProfessional_when_validId() {
        // Arrange
        when(profileRepository.findById(1L)).thenReturn(Optional.of(sampleProfile));
        when(profileRepository.save(any(ProfessionalProfile.class))).thenReturn(sampleProfile);

        // Act
        ProfessionalAdminDto result = adminProfessionalService.approve(1L);

        // Assert
        assertThat(result.approvalStatus()).isEqualTo("APPROVED");
        verify(profileRepository).save(any(ProfessionalProfile.class));
    }

    @Test
    void should_rejectProfessional_when_validId() {
        // Arrange
        when(profileRepository.findById(1L)).thenReturn(Optional.of(sampleProfile));
        when(profileRepository.save(any(ProfessionalProfile.class))).thenReturn(sampleProfile);

        // Act
        ProfessionalAdminDto result = adminProfessionalService.reject(1L);

        // Assert
        assertThat(result.approvalStatus()).isEqualTo("REJECTED");
        verify(profileRepository).save(any(ProfessionalProfile.class));
    }

    @Test
    void should_throwException_when_professionalNotFound() {
        // Arrange
        when(profileRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> adminProfessionalService.approve(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }
}
