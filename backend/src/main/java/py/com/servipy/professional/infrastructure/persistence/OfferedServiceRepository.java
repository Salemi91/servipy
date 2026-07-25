package py.com.servipy.professional.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import py.com.servipy.professional.domain.OfferedService;

public interface OfferedServiceRepository extends JpaRepository<OfferedService, Long> {
}
