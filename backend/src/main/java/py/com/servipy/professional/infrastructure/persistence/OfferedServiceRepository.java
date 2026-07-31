package py.com.servipy.professional.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import py.com.servipy.professional.domain.OfferedService;

import java.util.List;
import java.util.Optional;

public interface OfferedServiceRepository extends JpaRepository<OfferedService, Long> {

    List<OfferedService> findByProfessionalIdAndActiveTrueOrderByIdAsc(Long professionalId);

    Optional<OfferedService> findByIdAndProfessionalIdAndActiveTrue(Long id, Long professionalId);
}
