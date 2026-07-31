package py.com.servipy.servicerequest.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.servipy.professional.domain.ApprovalStatus;
import py.com.servipy.professional.domain.ProfessionalProfile;
import py.com.servipy.professional.infrastructure.persistence.ProfessionalProfileRepository;
import py.com.servipy.servicerequest.application.dto.ClientRequestView;
import py.com.servipy.servicerequest.application.dto.CreateServiceRequestDto;
import py.com.servipy.servicerequest.application.dto.CreateServiceRequestResponse;
import py.com.servipy.servicerequest.application.dto.ServiceRequestDetailDto;
import py.com.servipy.servicerequest.application.dto.ServiceRequestSummaryDto;
import py.com.servipy.servicerequest.domain.RequestStatus;
import py.com.servipy.servicerequest.domain.ServiceRequest;
import py.com.servipy.servicerequest.infrastructure.persistence.ServiceRequestRepository;
import py.com.servipy.shared.exception.InvalidStateTransitionException;
import py.com.servipy.shared.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class ServiceRequestService {

    private final ServiceRequestRepository requestRepository;
    private final ProfessionalProfileRepository professionalRepository;

    public ServiceRequestService(ServiceRequestRepository requestRepository,
                                  ProfessionalProfileRepository professionalRepository) {
        this.requestRepository = requestRepository;
        this.professionalRepository = professionalRepository;
    }

    public CreateServiceRequestResponse create(Long professionalId, CreateServiceRequestDto dto) {
        ProfessionalProfile professional = findActiveProfessional(professionalId);

        ServiceRequest request = new ServiceRequest();
        request.setProfessional(professional);
        request.setClientName(dto.name());
        request.setClientEmail(dto.email());
        request.setClientPhone(dto.phone());
        request.setSubject(dto.subject());
        request.setDescription(dto.description());
        request.setDesiredDate(dto.desiredDate());
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());

        ServiceRequest saved = requestRepository.save(request);
        return new CreateServiceRequestResponse(saved.getId());
    }

    /**
     * Lista las solicitudes del profesional indicado.
     * Solo el profesional dueño del perfil puede consultarlas.
     */
    @Transactional(readOnly = true)
    public List<ServiceRequestSummaryDto> findByProfessional(Long professionalId, Long authenticatedUserId,
                                                             RequestStatus status) {
        assertOwnership(professionalId, authenticatedUserId);

        List<ServiceRequest> requests;
        if (status != null) {
            requests = requestRepository.findByProfessionalIdAndStatusOrderByCreatedAtDesc(professionalId, status);
        } else {
            requests = requestRepository.findByProfessionalIdOrderByCreatedAtDesc(professionalId);
        }
        return requests.stream().map(this::toSummaryDto).toList();
    }

    /**
     * Devuelve el detalle de una solicitud del profesional indicado.
     * Solo el profesional dueño del perfil puede consultarla.
     */
    @Transactional(readOnly = true)
    public ServiceRequestDetailDto findDetail(Long professionalId, Long authenticatedUserId, Long requestId) {
        assertOwnership(professionalId, authenticatedUserId);

        ServiceRequest request = requestRepository.findByIdAndProfessionalId(requestId, professionalId)
            .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        return toDetailDto(request);
    }

    /**
     * Aplica una transición de estado sobre una solicitud del profesional indicado.
     * Solo el profesional dueño del perfil puede resolverla.
     */
    public void changeStatus(Long professionalId, Long authenticatedUserId, Long requestId,
                             RequestStatus targetStatus) {
        assertOwnership(professionalId, authenticatedUserId);

        ServiceRequest request = requestRepository.findByIdAndProfessionalId(requestId, professionalId)
            .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        if (!request.getStatus().canTransitionTo(targetStatus)) {
            throw new InvalidStateTransitionException(
                "No se puede cambiar de " + request.getStatus() + " a " + targetStatus
            );
        }

        request.setStatus(targetStatus);
        request.setUpdatedAt(Instant.now());
        requestRepository.save(request);
    }

    /**
     * Solicitudes enviadas por un cliente, identificado por el email de su cuenta.
     */
    @Transactional(readOnly = true)
    public Page<ClientRequestView> findByClientEmail(String clientEmail, RequestStatus status, Pageable pageable) {
        Page<ServiceRequest> page = status != null
            ? requestRepository.findByClientEmailIgnoreCaseAndStatusOrderByCreatedAtDesc(clientEmail, status, pageable)
            : requestRepository.findByClientEmailIgnoreCaseOrderByCreatedAtDesc(clientEmail, pageable);

        return page.map(this::toClientView);
    }

    /**
     * Solicitud concreta de un cliente. Devuelve 404 si no existe o si no le pertenece,
     * para no revelar la existencia de solicitudes ajenas.
     */
    @Transactional(readOnly = true)
    public ClientRequestView findByIdAndClientEmail(Long requestId, String clientEmail) {
        ServiceRequest request = requestRepository.findById(requestId)
            .filter(r -> r.getClientEmail().equalsIgnoreCase(clientEmail))
            .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        return toClientView(request);
    }

    private ClientRequestView toClientView(ServiceRequest request) {
        ProfessionalProfile professional = request.getProfessional();
        return new ClientRequestView(
            request.getId(),
            request.getSubject(),
            request.getDescription(),
            request.getDesiredDate(),
            request.getStatus(),
            professional.getUser().getName(),
            professional.getPhone(),
            professional.getWhatsapp(),
            request.getCreatedAt(),
            request.getUpdatedAt()
        );
    }

    /**
     * Verifica que el perfil profesional de la ruta pertenezca al usuario autenticado.
     * Ante cualquier discrepancia lanza 404 en lugar de 403, para no revelar
     * la existencia de recursos ajenos.
     */
    private void assertOwnership(Long professionalId, Long authenticatedUserId) {
        Long ownProfileId = professionalRepository.findByUserId(authenticatedUserId)
            .map(ProfessionalProfile::getId)
            .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        if (!ownProfileId.equals(professionalId)) {
            throw new ResourceNotFoundException("Solicitud no encontrada");
        }
    }

    private ProfessionalProfile findActiveProfessional(Long professionalId) {
        return professionalRepository.findById(professionalId)
            .filter(p -> p.getUser().getActive() && p.getApprovalStatus() == ApprovalStatus.APPROVED)
            .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado"));
    }

    private ServiceRequestSummaryDto toSummaryDto(ServiceRequest r) {
        return new ServiceRequestSummaryDto(
            r.getId(), r.getClientName(), r.getSubject(),
            r.getStatus().name(), r.getCreatedAt()
        );
    }

    private ServiceRequestDetailDto toDetailDto(ServiceRequest r) {
        return new ServiceRequestDetailDto(
            r.getId(), r.getClientName(), r.getClientEmail(),
            r.getClientPhone(), r.getSubject(), r.getDescription(),
            r.getDesiredDate(), r.getStatus().name(),
            r.getCreatedAt(), r.getUpdatedAt()
        );
    }
}
