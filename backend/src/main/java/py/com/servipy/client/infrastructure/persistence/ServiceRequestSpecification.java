package py.com.servipy.client.infrastructure.persistence;

import org.springframework.data.jpa.domain.Specification;

import py.com.servipy.client.domain.RequestStatus;
import py.com.servipy.client.domain.ServiceRequest;

public final class ServiceRequestSpecification {

    private ServiceRequestSpecification() {
    }

    public static Specification<ServiceRequest> build(String clientEmail, RequestStatus status) {
        return Specification.where(byClientEmail(clientEmail))
                .and(byStatus(status));
    }

    private static Specification<ServiceRequest> byClientEmail(String clientEmail) {
        if (clientEmail == null || clientEmail.isBlank()) return null;
        return (root, query, cb) ->
                cb.equal(root.get("clientEmail"), clientEmail);
    }

    private static Specification<ServiceRequest> byStatus(RequestStatus status) {
        if (status == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }
}
