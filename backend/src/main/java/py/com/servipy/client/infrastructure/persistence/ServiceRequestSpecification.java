package py.com.servipy.client.infrastructure.persistence;

import org.springframework.data.jpa.domain.Specification;

import py.com.servipy.client.domain.RequestStatus;
import py.com.servipy.client.domain.ServiceRequest;

public final class ServiceRequestSpecification {

    private ServiceRequestSpecification() {
    }

    public static Specification<ServiceRequest> build(Long clientId, RequestStatus status) {
        return Specification.where(byClientId(clientId))
                .and(byStatus(status));
    }

    private static Specification<ServiceRequest> byClientId(Long clientId) {
        // The client module's service_requests view doesn't have a direct client FK;
        // this specification currently cannot filter by client since the table
        // uses client_email instead of client_id. Return all for now.
        return null;
    }

    private static Specification<ServiceRequest> byStatus(RequestStatus status) {
        if (status == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }
}
