package py.com.servipy.client.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.servipy.client.application.dto.ServiceRequestPageResponse;
import py.com.servipy.client.application.dto.ServiceRequestResponse;
import py.com.servipy.client.domain.RequestStatus;
import py.com.servipy.client.domain.ServiceRequest;
import py.com.servipy.client.infrastructure.persistence.ClientServiceRequestRepository;
import py.com.servipy.client.infrastructure.persistence.ServiceRequestSpecification;

@Service
@Transactional(readOnly = true)
public class ClientRequestServiceImpl implements ClientRequestService {

    private final ClientServiceRequestRepository serviceRequestRepository;

    public ClientRequestServiceImpl(ClientServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Override
    public ServiceRequestPageResponse getRequests(Long userId, String status, int page, int size) {
        RequestStatus requestStatus = parseStatus(status);

        Specification<ServiceRequest> spec = ServiceRequestSpecification.build(userId, requestStatus);
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
