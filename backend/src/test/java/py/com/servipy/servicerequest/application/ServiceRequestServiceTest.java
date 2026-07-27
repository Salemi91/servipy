package py.com.servipy.servicerequest.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import py.com.servipy.professional.domain.ApprovalStatus;
import py.com.servipy.professional.domain.Availability;
import py.com.servipy.professional.domain.ProfessionalProfile;
import py.com.servipy.professional.infrastructure.persistence.ProfessionalProfileRepository;
import py.com.servipy.servicerequest.application.dto.CreateServiceRequestDto;
import py.com.servipy.servicerequest.application.dto.CreateServiceRequestResponse;
import py.com.servipy.servicerequest.application.dto.ServiceRequestDetailDto;
import py.com.servipy.servicerequest.application.dto.ServiceRequestSummaryDto;
import py.com.servipy.servicerequest.domain.RequestStatus;
import py.com.servipy.servicerequest.domain.ServiceRequest;
import py.com.servipy.servicerequest.infrastructure.persistence.ServiceRequestRepository;
import py.com.servipy.shared.exception.InvalidStateTransitionException;
import py.com.servipy.shared.exception.ResourceNotFoundException;
import py.com.servipy.user.domain.User;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceTest {

    @Mock
    private ServiceRequestRepository requestRepository;

    @Mock
    private ProfessionalProfileRepository professionalRepository;

    @InjectMocks
    private ServiceRequestService service;

    // --- 6.1 Probar solicitud válida ---

    @Test
    void should_createRequest_when_professionalIsActiveAndApproved() {
        // Arrange
        Long professionalId = 1L;
        ProfessionalProfile professional = buildActiveProfessional(professionalId);
        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));

        CreateServiceRequestDto dto = new CreateServiceRequestDto(
            "Juan Pérez", "juan@example.com", "0981123456",
            "Reparación de cañería", "Necesito reparar una cañería en el baño",
            LocalDate.of(2025, 2, 15)
        );

        ServiceRequest savedRequest = new ServiceRequest();
        savedRequest.setId(10L);
        savedRequest.setStatus(RequestStatus.PENDING);
        when(requestRepository.save(any(ServiceRequest.class))).thenReturn(savedRequest);

        // Act
        CreateServiceRequestResponse response = service.create(professionalId, dto);

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.id());
        verify(requestRepository).save(any(ServiceRequest.class));
    }

    // --- 6.3 Probar profesional inexistente ---

    @Test
    void should_throw404_when_professionalNotFound() {
        // Arrange
        Long professionalId = 999L;
        when(professionalRepository.findById(professionalId)).thenReturn(Optional.empty());

        CreateServiceRequestDto dto = buildValidDto();

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> service.create(professionalId, dto)
        );
        assertEquals("Profesional no encontrado", ex.getMessage());
    }

    @Test
    void should_throw404_when_professionalNotActive() {
        // Arrange
        Long professionalId = 1L;
        ProfessionalProfile professional = buildProfessional(professionalId, false, ApprovalStatus.APPROVED);
        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));

        CreateServiceRequestDto dto = buildValidDto();

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> service.create(professionalId, dto)
        );
        assertEquals("Profesional no encontrado", ex.getMessage());
    }

    @Test
    void should_throw404_when_professionalNotApproved() {
        // Arrange
        Long professionalId = 1L;
        ProfessionalProfile professional = buildProfessional(professionalId, true, ApprovalStatus.PENDING);
        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));

        CreateServiceRequestDto dto = buildValidDto();

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> service.create(professionalId, dto)
        );
        assertEquals("Profesional no encontrado", ex.getMessage());
    }

    // --- 6.4 Probar listado por profesional ---

    @Test
    void should_returnOnlyOwnRequests_when_listByProfessional() {
        // Arrange
        Long professionalId = 1L;
        ServiceRequest req1 = buildServiceRequest(1L, professionalId, "Cliente A", "Asunto A", RequestStatus.PENDING);
        ServiceRequest req2 = buildServiceRequest(2L, professionalId, "Cliente B", "Asunto B", RequestStatus.ACCEPTED);

        when(requestRepository.findByProfessionalIdOrderByCreatedAtDesc(professionalId))
            .thenReturn(List.of(req1, req2));

        // Act
        List<ServiceRequestSummaryDto> result = service.findByProfessional(professionalId, null);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Cliente A", result.get(0).clientName());
        assertEquals("Cliente B", result.get(1).clientName());
        verify(requestRepository).findByProfessionalIdOrderByCreatedAtDesc(professionalId);
    }

    // --- 6.5 Probar detalle ---

    @Test
    void should_returnDetail_when_requestExistsForProfessional() {
        // Arrange
        Long professionalId = 1L;
        Long requestId = 5L;
        ServiceRequest request = buildServiceRequest(requestId, professionalId, "María", "Consulta", RequestStatus.PENDING);
        request.setClientEmail("maria@example.com");
        request.setDescription("Descripción de prueba");

        when(requestRepository.findByIdAndProfessionalId(requestId, professionalId))
            .thenReturn(Optional.of(request));

        // Act
        ServiceRequestDetailDto detail = service.findDetail(professionalId, requestId);

        // Assert
        assertNotNull(detail);
        assertEquals(requestId, detail.id());
        assertEquals("María", detail.clientName());
        assertEquals("maria@example.com", detail.clientEmail());
        assertEquals("PENDING", detail.status());
    }

    @Test
    void should_throw404_when_requestNotFound() {
        // Arrange
        Long professionalId = 1L;
        Long requestId = 999L;
        when(requestRepository.findByIdAndProfessionalId(requestId, professionalId))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> service.findDetail(professionalId, requestId)
        );
        assertEquals("Solicitud no encontrada", ex.getMessage());
    }

    @Test
    void should_throw404_when_requestBelongsToOtherProfessional() {
        // Arrange
        Long professionalId = 1L;
        Long otherProfessionalId = 2L;
        Long requestId = 5L;

        // The request belongs to professional 2, but we query with professional 1
        when(requestRepository.findByIdAndProfessionalId(requestId, professionalId))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> service.findDetail(professionalId, requestId)
        );
        assertEquals("Solicitud no encontrada", ex.getMessage());
    }

    // --- 6.6 Probar aceptación ---

    @Test
    void should_acceptRequest_when_statusIsPending() {
        // Arrange
        Long professionalId = 1L;
        Long requestId = 5L;
        ServiceRequest request = buildServiceRequest(requestId, professionalId, "Carlos", "Pintura", RequestStatus.PENDING);

        when(requestRepository.findByIdAndProfessionalId(requestId, professionalId))
            .thenReturn(Optional.of(request));
        when(requestRepository.save(any(ServiceRequest.class))).thenReturn(request);

        // Act
        service.changeStatus(professionalId, requestId, RequestStatus.ACCEPTED);

        // Assert
        assertEquals(RequestStatus.ACCEPTED, request.getStatus());
        verify(requestRepository).save(request);
    }

    // --- 6.7 Probar rechazo ---

    @Test
    void should_rejectRequest_when_statusIsPending() {
        // Arrange
        Long professionalId = 1L;
        Long requestId = 5L;
        ServiceRequest request = buildServiceRequest(requestId, professionalId, "Ana", "Electricidad", RequestStatus.PENDING);

        when(requestRepository.findByIdAndProfessionalId(requestId, professionalId))
            .thenReturn(Optional.of(request));
        when(requestRepository.save(any(ServiceRequest.class))).thenReturn(request);

        // Act
        service.changeStatus(professionalId, requestId, RequestStatus.REJECTED);

        // Assert
        assertEquals(RequestStatus.REJECTED, request.getStatus());
        verify(requestRepository).save(request);
    }

    // --- 6.8 Probar transición inválida ---

    @Test
    void should_throw409_when_statusIsAccepted() {
        // Arrange
        Long professionalId = 1L;
        Long requestId = 5L;
        ServiceRequest request = buildServiceRequest(requestId, professionalId, "Pedro", "Plomería", RequestStatus.ACCEPTED);

        when(requestRepository.findByIdAndProfessionalId(requestId, professionalId))
            .thenReturn(Optional.of(request));

        // Act & Assert
        assertThrows(
            InvalidStateTransitionException.class,
            () -> service.changeStatus(professionalId, requestId, RequestStatus.REJECTED)
        );
        verify(requestRepository, never()).save(any());
    }

    @Test
    void should_throw409_when_statusIsRejected() {
        // Arrange
        Long professionalId = 1L;
        Long requestId = 5L;
        ServiceRequest request = buildServiceRequest(requestId, professionalId, "Luis", "Limpieza", RequestStatus.REJECTED);

        when(requestRepository.findByIdAndProfessionalId(requestId, professionalId))
            .thenReturn(Optional.of(request));

        // Act & Assert
        assertThrows(
            InvalidStateTransitionException.class,
            () -> service.changeStatus(professionalId, requestId, RequestStatus.ACCEPTED)
        );
        verify(requestRepository, never()).save(any());
    }

    // --- Helper methods ---

    private ProfessionalProfile buildActiveProfessional(Long id) {
        return buildProfessional(id, true, ApprovalStatus.APPROVED);
    }

    private ProfessionalProfile buildProfessional(Long id, boolean active, ApprovalStatus approvalStatus) {
        User user = new User("Professional " + id, "pro" + id + "@example.com", "hash", py.com.servipy.user.domain.Role.PROFESSIONAL);
        user.setId(id);
        user.setActive(active);

        ProfessionalProfile professional = new ProfessionalProfile();
        professional.setId(id);
        professional.setUser(user);
        professional.setApprovalStatus(approvalStatus);
        professional.setAvailability(Availability.PRESENCIAL);
        return professional;
    }

    private CreateServiceRequestDto buildValidDto() {
        return new CreateServiceRequestDto(
            "Juan Pérez", "juan@example.com", "0981123456",
            "Reparación general", "Necesito un servicio de reparación",
            LocalDate.of(2025, 3, 1)
        );
    }

    private ServiceRequest buildServiceRequest(Long id, Long professionalId, String clientName,
                                                String subject, RequestStatus status) {
        ProfessionalProfile professional = new ProfessionalProfile();
        professional.setId(professionalId);

        ServiceRequest request = new ServiceRequest();
        request.setId(id);
        request.setProfessional(professional);
        request.setClientName(clientName);
        request.setClientEmail(clientName.toLowerCase().replace(" ", "") + "@example.com");
        request.setSubject(subject);
        request.setDescription("Descripción de prueba para " + subject);
        request.setStatus(status);
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());
        return request;
    }
}
