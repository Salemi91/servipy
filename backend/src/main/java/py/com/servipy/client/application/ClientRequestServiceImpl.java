package py.com.servipy.client.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.servipy.client.application.dto.ServiceRequestDetailResponse;
import py.com.servipy.client.application.dto.ServiceRequestPageResponse;
import py.com.servipy.client.application.dto.ServiceRequestResponse;
import py.com.servipy.client.domain.RequestStatus;
import py.com.servipy.client.domain.ServiceRequest;
import py.com.servipy.client.infrastructure.persistence.ClientServiceRequestRepository;
import py.com.servipy.client.infrastructure.persistence.ServiceRequestSpecification;
import py.com.servipy.servicerequest.infrastructure.persistence.ServiceRequestRepository;
import py.com.servipy.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
public class ClientRequestServiceImpl implements ClientRequestService {

    private final ClientServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestRepository mainServiceRequestRepository;

    public ClientRequestServiceImpl(
            ClientServiceRequestRepository serviceRequestRepository,
            ServiceRequestRepository mainServiceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.mainServiceRequestRepository = mainServiceRequestRepository;
    }

    @Override
    public ServiceRequestPageResponse getRequests(String clientEmail, String status, int page, int size) {
        RequestStatus requestStatus = parseStatus(status);

        Specification<ServiceRequest> spec = ServiceRequestSpecification.build(clientEmail, requestStatus);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ServiceRequest> resultPage = serviceRequestRepository.findAll(spec, pageable);

        return new ServiceRequestPageResponse(
            resultPage.getContent().stream()
                .map(this::toResponse)
                .toList(),
            resultPage.getTotalElements(),
            resultPage.getTotalPages(),
            resultPage.getNumber(),
            resultPage.getSize()
        );
    }

    @Override
    public ServiceRequestDetailResponse getRequestDetail(Long requestId, String clientEmail) {
        py.com.servipy.servicerequest.domain.ServiceRequest entity =
            mainServiceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        // Verificar que la solicitud pertenece al usuario autenticado
        if (!entity.getClientEmail().equalsIgnoreCase(clientEmail)) {
            throw new ResourceNotFoundException("Solicitud no encontrada");
        }

        String professionalName = entity.getProfessional().getUser().getName();
        String desiredDate = entity.getDesiredDate() != null ? entity.getDesiredDate().toString() : null;

        return new ServiceRequestDetailResponse(
            entity.getId(),
            professionalName,
            entity.getSubject(),
            entity.getDescription(),
            desiredDate,
            entity.getStatus().name(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private RequestStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return RequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Valores de estado permitidos: PENDING, ACCEPTED, REJECTED"
            );
        }
    }

    private ServiceRequestResponse toResponse(ServiceRequest entity) {
        return new ServiceRequestResponse(
            entity.getId(),
            entity.getServiceName(),
            entity.getProfessionalName(),
            entity.getStatus().name(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
