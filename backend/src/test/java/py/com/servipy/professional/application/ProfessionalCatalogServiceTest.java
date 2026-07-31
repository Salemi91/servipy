package py.com.servipy.professional.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import py.com.servipy.category.domain.Category;
import py.com.servipy.city.domain.City;
import py.com.servipy.professional.application.dto.ProfessionalDetailDto;
import py.com.servipy.professional.application.dto.ProfessionalSummaryDto;
import py.com.servipy.professional.domain.ApprovalStatus;
import py.com.servipy.professional.domain.Availability;
import py.com.servipy.professional.domain.OfferedService;
import py.com.servipy.professional.domain.ProfessionalProfile;
import py.com.servipy.professional.infrastructure.persistence.ProfessionalProfileRepository;
import py.com.servipy.shared.exception.ResourceNotFoundException;
import py.com.servipy.user.domain.Role;
import py.com.servipy.user.domain.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalCatalogServiceTest {

    @Mock
    private ProfessionalProfileRepository profileRepository;


    @InjectMocks
    private ProfessionalCatalogService catalogService;

    private User activeUser;
    private City city;
    private Category category;
    private ProfessionalProfile approvedProfile;
    private OfferedService activeService;

    @BeforeEach
    void setUp() {
        activeUser = new User("Juan Pérez", "juan@test.com", "hash", Role.PROFESSIONAL);
        activeUser.setId(1L);

        city = new City();
        city.setId(1L);
        city.setName("Asunción");

        category = new Category();
        category.setId(1L);
        category.setName("Electricidad");
        category.setActive(true);

        approvedProfile = new ProfessionalProfile();
        approvedProfile.setId(1L);
        approvedProfile.setUser(activeUser);
        approvedProfile.setCity(city);
        approvedProfile.setApprovalStatus(ApprovalStatus.APPROVED);
        approvedProfile.setAvailability(Availability.PRESENCIAL);
        approvedProfile.setDescription("Electricista profesional con 10 años de experiencia");
        approvedProfile.setPhone("+595981123456");
        approvedProfile.setWhatsapp("+595981123456");
        approvedProfile.setPhotoUrl("https://example.com/photo.jpg");
        approvedProfile.setCreatedAt(Instant.now());
        approvedProfile.setUpdatedAt(Instant.now());

        activeService = new OfferedService();
        activeService.setId(1L);
        activeService.setProfessional(approvedProfile);
        activeService.setCategory(category);
        activeService.setName("Instalación eléctrica");
        activeService.setDescription("Instalación completa de cableado");
        activeService.setPrice(new BigDecimal("150000"));
        activeService.setCurrency("PYG");
        activeService.setActive(true);

        approvedProfile.setOfferedServices(List.of(activeService));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_returnOnlyApprovedProfessionals_when_listAll() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<ProfessionalProfile> page = new PageImpl<>(List.of(approvedProfile), pageable, 1);
        when(profileRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ProfessionalSummaryDto> result = catalogService.findAll(null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Juan Pérez");
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_filterByCategory_when_categoryIdProvided() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<ProfessionalProfile> page = new PageImpl<>(List.of(approvedProfile), pageable, 1);
        when(profileRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ProfessionalSummaryDto> result = catalogService.findAll(1L, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_filterByCity_when_cityIdProvided() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<ProfessionalProfile> page = new PageImpl<>(List.of(approvedProfile), pageable, 1);
        when(profileRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ProfessionalSummaryDto> result = catalogService.findAll(null, 5L, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).cityName()).isEqualTo("Asunción");
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_returnEmpty_when_categoryIdNotExists() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<ProfessionalProfile> page = new PageImpl<>(List.of(), pageable, 0);
        when(profileRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ProfessionalSummaryDto> result = catalogService.findAll(999L, null, null, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_filterBySearch_when_searchProvided() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<ProfessionalProfile> page = new PageImpl<>(List.of(approvedProfile), pageable, 1);
        when(profileRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ProfessionalSummaryDto> result = catalogService.findAll(null, null, "electricista", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_applyBothFilters_when_searchAndCategoryProvided() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<ProfessionalProfile> page = new PageImpl<>(List.of(approvedProfile), pageable, 1);
        when(profileRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ProfessionalSummaryDto> result = catalogService.findAll(1L, null, "electricista", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_ignoreSearch_when_searchIsBlank() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<ProfessionalProfile> page = new PageImpl<>(List.of(approvedProfile), pageable, 1);
        when(profileRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ProfessionalSummaryDto> result = catalogService.findAll(null, null, "   ", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void should_returnDetail_when_professionalIsActive() {
        when(profileRepository.findActiveById(1L)).thenReturn(Optional.of(approvedProfile));

        ProfessionalDetailDto result = catalogService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Juan Pérez");
        assertThat(result.cityName()).isEqualTo("Asunción");
        assertThat(result.services()).hasSize(1);
        assertThat(result.services().get(0).name()).isEqualTo("Instalación eléctrica");
    }

    @Test
    void should_returnCountsByCategory_when_categoriesHaveVisibleProfessionals() {
        when(profileRepository.countActiveProfessionalsByCategory())
            .thenReturn(List.of(
                new Object[]{1L, 3L},
                new Object[]{2L, 1L}
            ));

        var result = catalogService.countByCategory();

        assertThat(result).containsExactlyInAnyOrderEntriesOf(
            java.util.Map.of(1L, 3L, 2L, 1L)
        );
    }

    @Test
    void should_returnEmptyMap_when_noCategoryHasVisibleProfessionals() {
        when(profileRepository.countActiveProfessionalsByCategory()).thenReturn(List.of());

        var result = catalogService.countByCategory();

        assertThat(result).isEmpty();
    }

    @Test
    void should_throw404_when_professionalNotFound() {
        when(profileRepository.findActiveById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Profesional no encontrado");
    }

    @Test
    void should_throw404_when_professionalNotApproved() {
        when(profileRepository.findActiveById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.findById(2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_truncateDescription_when_exceedsMaxLength() {
        String longDescription = "A".repeat(200);
        approvedProfile.setDescription(longDescription);

        Pageable pageable = PageRequest.of(0, 12);
        Page<ProfessionalProfile> page = new PageImpl<>(List.of(approvedProfile), pageable, 1);
        when(profileRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ProfessionalSummaryDto> result = catalogService.findAll(null, null, null, pageable);

        String description = result.getContent().get(0).description();
        assertThat(description).hasSize(150);
        assertThat(description).endsWith("...");
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_returnMinPrice_when_multipleServicesExist() {
        OfferedService cheapService = new OfferedService();
        cheapService.setId(2L);
        cheapService.setProfessional(approvedProfile);
        cheapService.setCategory(category);
        cheapService.setName("Reparación menor");
        cheapService.setDescription("Reparación básica");
        cheapService.setPrice(new BigDecimal("50000"));
        cheapService.setCurrency("PYG");
        cheapService.setActive(true);

        OfferedService expensiveService = new OfferedService();
        expensiveService.setId(3L);
        expensiveService.setProfessional(approvedProfile);
        expensiveService.setCategory(category);
        expensiveService.setName("Instalación completa");
        expensiveService.setDescription("Instalación total");
        expensiveService.setPrice(new BigDecimal("500000"));
        expensiveService.setCurrency("PYG");
        expensiveService.setActive(true);

        approvedProfile.setOfferedServices(List.of(activeService, cheapService, expensiveService));

        Pageable pageable = PageRequest.of(0, 12);
        Page<ProfessionalProfile> page = new PageImpl<>(List.of(approvedProfile), pageable, 1);
        when(profileRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ProfessionalSummaryDto> result = catalogService.findAll(null, null, null, pageable);

        assertThat(result.getContent().get(0).referencePrice())
                .isEqualByComparingTo(new BigDecimal("50000"));
    }
}
