package py.com.servipy.client.application;

import py.com.servipy.client.application.dto.ProfessionalContactResponse;
import py.com.servipy.client.application.dto.ServiceRequestDetailResponse;
import py.com.servipy.client.application.dto.ServiceRequestPageResponse;

public interface ClientRequestService {

    ServiceRequestPageResponse getRequests(String clientEmail, String status, int page, int size);

    ServiceRequestDetailResponse getRequestDetail(Long requestId, String clientEmail);

    /**
     * Datos de contacto del profesional asociado a una solicitud aceptada del cliente.
     */
    ProfessionalContactResponse getProfessionalContact(Long requestId, String clientEmail);
}
