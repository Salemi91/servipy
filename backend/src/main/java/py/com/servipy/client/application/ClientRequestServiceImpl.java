package py.com.servipy.client.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.servipy.client.application.dto.ProfessionalContactResponse;
import py.com.servipy.client.application.dto.ServiceRequestDetailResponse;
import py.com.servipy.client.application.dto.ServiceRequestPageResponse;
import py.com.servipy.client.application.dto.ServiceRequestResponse;
import py.com.servipy.client.application.exception.ContactNotAvailableException;
import py.com.servipy.servicerequest.application.ServiceRequestService;
import py.com.servipy.servicerequest.application.dto.ClientRequestView;
import py.com.servipy.servicerequest.domain.RequestStatus;

/**
 * Consultas del cliente sobre sus propias solicitudes.
 * Los datos pertenecen al slice servicerequest: se consumen a través de su
 * capa de aplicación y se traducen a los DTOs del contrato del cliente.
 */
@Service
@Transactional(readOnly = true)
public class ClientRequestServiceImpl implements ClientRequestService {

    private final ServiceRequestService serviceRequestService;

    public ClientRequestServiceImpl(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    @Override
    public ServiceRequestPageResponse getRequests(String clientEmail, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ClientRequestView> resultPage =
            serviceRequestService.findByClientEmail(clientEmail, parseStatus(status), pageable);

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
        ClientRequestView view = serviceRequestService.findByIdAndClientEmail(requestId, clientEmail);

        return new ServiceRequestDetailResponse(
            view.id(),
            view.professionalName(),
            view.subject(),
            view.description(),
            view.desiredDate() != null ? view.desiredDate().toString() : null,
            view.status().name(),
            view.createdAt(),
            view.updatedAt()
        );
    }

    @Override
    public ProfessionalContactResponse getProfessionalContact(Long requestId, String clientEmail) {
        ClientRequestView view = serviceRequestService.findByIdAndClientEmail(requestId, clientEmail);

        if (view.status() != RequestStatus.ACCEPTED) {
            throw new ContactNotAvailableException(
                "Los datos de contacto están disponibles cuando el profesional acepta la solicitud");
        }

        return new ProfessionalContactResponse(
            view.professionalName(),
            view.professionalPhone(),
            view.professionalWhatsapp()
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

    private ServiceRequestResponse toResponse(ClientRequestView view) {
        return new ServiceRequestResponse(
            view.id(),
            view.subject(),
            view.professionalName(),
            view.status().name(),
            view.createdAt(),
            view.updatedAt()
        );
    }
}
