package py.com.servipy.client.application;

import py.com.servipy.client.application.dto.ServiceRequestDetailResponse;
import py.com.servipy.client.application.dto.ServiceRequestPageResponse;

public interface ClientRequestService {
    ServiceRequestPageResponse getRequests(String clientEmail, String status, int page, int size);

    ServiceRequestDetailResponse getRequestDetail(Long requestId, String clientEmail);
}
