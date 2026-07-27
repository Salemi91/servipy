package py.com.servipy.client.application;

import py.com.servipy.client.application.dto.ServiceRequestPageResponse;

public interface ClientRequestService {
    ServiceRequestPageResponse getRequests(Long userId, String status, int page, int size);
}
