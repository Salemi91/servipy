package py.com.servipy.servicerequest.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.servipy.professional.domain.ApprovalStatus;
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

    @Transactional(readOnly = true)
    public List<ServiceRequestSummaryDto> findByProfessional(Long professionalId, RequestStatus status) {
        List<ServiceRequest> requests;
        if (status != null) {
            requests = requestRepository.findByProfessionalIdAndStatusOrderByCreatedAtDesc(professionalId, status);
        } else {
            requests = requestRepository.findByProfessionalIdOrderByCreatedAtDesc(professionalId);
        }
        return requests.stream().map(this::toSummaryDto).toList();
    }

    @Transactional(readOnly = true)
    public ServiceRequestDetailDto findDetail(Long professionalId, Long requestId) {
        ServiceRequest request = requestRepository.findByIdAndProfessionalId(requestId, professionalId)
            .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        return toDetailDto(request);
    }

    public void changeStatus(Long professionalId, Long requestId, RequestStatus targetStatus) {
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
