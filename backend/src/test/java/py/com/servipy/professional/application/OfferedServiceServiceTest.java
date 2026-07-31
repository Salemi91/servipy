package py.com.servipy.professional.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import py.com.servipy.category.application.CategoryService;
import py.com.servipy.category.domain.Category;
import py.com.servipy.professional.application.dto.CreateOfferedServiceRequest;
import py.com.servipy.professional.application.dto.OfferedServiceDto;
import py.com.servipy.professional.domain.OfferedService;
import py.com.servipy.professional.domain.ProfessionalProfile;
import py.com.servipy.professional.infrastructure.persistence.OfferedServiceRepository;
import py.com.servipy.professional.infrastructure.persistence.ProfessionalProfileRepository;
import py.com.servipy.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferedServiceServiceTest {

    private static final Long USER_ID = 100L;
    private static final Long PROFILE_ID = 7L;

    @Mock
    private OfferedServiceRepository offeredServiceRepository;

    @Mock
    private ProfessionalProfileRepository profileRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private OfferedServiceService service;

    @Test
    void should_createActiveService_when_professionalHasProfile() {
        // Arrange
        stubOwnProfile();
        when(categoryService.findActiveEntityById(3L)).thenReturn(buildCategory("Electricidad"));
        when(offeredServiceRepository.save(any(OfferedService.class))).thenAnswer(i -> i.getArgument(0));

        CreateOfferedServiceRequest request = new CreateOfferedServiceRequest(
            3L, "  Instalación eléctrica  ", "  Domiciliaria  ", new BigDecimal("150000"), null);

        // Act
        OfferedServiceDto result = service.create(USER_ID, request);

        // Assert
        ArgumentCaptor<OfferedService> captor = ArgumentCaptor.forClass(OfferedService.class);
        verify(offeredServiceRepository).save(captor.capture());
        OfferedService saved = captor.getValue();

        assertThat(saved.getActive()).isTrue();
        assertThat(saved.getName()).isEqualTo("Instalación eléctrica");
        assertThat(saved.getDescription()).isEqualTo("Domiciliaria");
        assertThat(saved.getProfessional().getId()).isEqualTo(PROFILE_ID);
        assertThat(result.currency()).isEqualTo("PYG");
        assertThat(result.categoryName()).isEqualTo("Electricidad");
    }

    @Test
    void should_useProvidedCurrency_when_currencyIsInformed() {
        stubOwnProfile();
        when(categoryService.findActiveEntityById(3L)).thenReturn(buildCategory("Electricidad"));
        when(offeredServiceRepository.save(any(OfferedService.class))).thenAnswer(i -> i.getArgument(0));

        CreateOfferedServiceRequest request = new CreateOfferedServiceRequest(
            3L, "Cableado", null, new BigDecimal("90000"), "usd");

        OfferedServiceDto result = service.create(USER_ID, request);

        assertThat(result.currency()).isEqualTo("USD");
    }

    @Test
    void should_throw404_when_userHasNoProfessionalProfile() {
        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        CreateOfferedServiceRequest request = new CreateOfferedServiceRequest(
            3L, "Cableado", null, new BigDecimal("90000"), null);

        assertThatThrownBy(() -> service.create(USER_ID, request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Perfil profesional no encontrado");

        verify(offeredServiceRepository, never()).save(any());
    }

    @Test
    void should_returnOnlyActiveServicesOfOwnProfile_when_listing() {
        stubOwnProfile();
        when(offeredServiceRepository.findByProfessionalIdAndActiveTrueOrderByIdAsc(PROFILE_ID))
            .thenReturn(List.of(buildOfferedService(1L, "Cableado")));

        List<OfferedServiceDto> result = service.findOwnServices(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Cableado");
        verify(offeredServiceRepository).findByProfessionalIdAndActiveTrueOrderByIdAsc(PROFILE_ID);
    }

    @Test
    void should_deactivateService_when_serviceBelongsToProfessional() {
        stubOwnProfile();
        OfferedService offeredService = buildOfferedService(5L, "Cableado");
        when(offeredServiceRepository.findByIdAndProfessionalIdAndActiveTrue(5L, PROFILE_ID))
            .thenReturn(Optional.of(offeredService));

        service.deactivate(USER_ID, 5L);

        assertThat(offeredService.getActive()).isFalse();
        verify(offeredServiceRepository).save(offeredService);
    }

    @Test
    void should_throw404_when_deactivatingServiceOfAnotherProfessional() {
        stubOwnProfile();
        when(offeredServiceRepository.findByIdAndProfessionalIdAndActiveTrue(5L, PROFILE_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivate(USER_ID, 5L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Servicio no encontrado");

        verify(offeredServiceRepository, never()).save(any());
    }

    // --- Helpers ---

    private void stubOwnProfile() {
        ProfessionalProfile profile = new ProfessionalProfile();
        profile.setId(PROFILE_ID);
        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));
    }

    private Category buildCategory(String name) {
        Category category = new Category();
        category.setId(3L);
        category.setName(name);
        category.setActive(true);
        return category;
    }

    private OfferedService buildOfferedService(Long id, String name) {
        ProfessionalProfile profile = new ProfessionalProfile();
        profile.setId(PROFILE_ID);

        OfferedService offeredService = new OfferedService();
        offeredService.setId(id);
        offeredService.setProfessional(profile);
        offeredService.setCategory(buildCategory("Electricidad"));
        offeredService.setName(name);
        offeredService.setPrice(new BigDecimal("90000"));
        offeredService.setCurrency("PYG");
        offeredService.setActive(true);
        return offeredService;
    }
}
