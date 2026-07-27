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
        return (root, query, cb) ->
                cb.equal(root.get("client").get("id"), clientId);
    }

    private static Specification<ServiceRequest> byStatus(RequestStatus status) {
        if (status == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }
}
