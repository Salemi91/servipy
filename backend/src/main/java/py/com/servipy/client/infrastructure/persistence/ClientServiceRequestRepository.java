package py.com.servipy.client.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import py.com.servipy.client.domain.ServiceRequest;

public interface ClientServiceRequestRepository extends JpaRepository<ServiceRequest, Long>,
        JpaSpecificationExecutor<ServiceRequest> {
}
