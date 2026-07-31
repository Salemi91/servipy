package py.com.servipy.professional.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import py.com.servipy.professional.domain.ApprovalStatus;
import py.com.servipy.professional.domain.ProfessionalProfile;

import java.util.List;
import java.util.Optional;

public interface ProfessionalProfileRepository extends JpaRepository<ProfessionalProfile, Long>,
        JpaSpecificationExecutor<ProfessionalProfile> {

    /**
     * Busca un perfil aprobado con usuario activo y al menos un servicio activo.
     * Usa fetch joins para evitar N+1.
     */
    @Query("""
        SELECT DISTINCT p FROM ProfessionalProfile p
        JOIN FETCH p.user u
        LEFT JOIN FETCH p.city c
        LEFT JOIN FETCH p.offeredServices os
        LEFT JOIN FETCH os.category
        WHERE p.id = :id
          AND p.approvalStatus = 'APPROVED'
          AND u.active = true
          AND os.active = true
    """)
    Optional<ProfessionalProfile> findActiveById(@Param("id") Long id);

    List<ProfessionalProfile> findByApprovalStatus(ApprovalStatus approvalStatus);

    Optional<ProfessionalProfile> findByUserId(Long userId);

    /**
     * Cuenta profesionales visibles en el catálogo (aprobados, activos, con
     * servicio activo) agrupados por categoría. Usado para mostrar
     * "N profesionales disponibles" en las categorías populares del home.
     */
    @Query("""
        SELECT os.category.id, COUNT(DISTINCT p.id)
        FROM ProfessionalProfile p
        JOIN p.offeredServices os
        WHERE p.approvalStatus = 'APPROVED'
          AND p.user.active = true
          AND os.active = true
        GROUP BY os.category.id
    """)
    List<Object[]> countActiveProfessionalsByCategory();
}
