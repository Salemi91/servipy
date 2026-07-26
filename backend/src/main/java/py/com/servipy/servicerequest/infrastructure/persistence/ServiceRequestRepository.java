package py.com.servipy.servicerequest.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import py.com.servipy.servicerequest.domain.RequestStatus;
import py.com.servipy.servicerequest.domain.ServiceRequest;

import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByProfessionalIdOrderByCreatedAtDesc(Long professionalId);

    List<ServiceRequest> findByProfessionalIdAndStatusOrderByCreatedAtDesc(Long professionalId, RequestStatus status);

    Optional<ServiceRequest> findByIdAndProfessionalId(Long id, Long professionalId);
}
