package py.com.servipy.professional.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import py.com.servipy.professional.application.AdminProfessionalService;
import py.com.servipy.professional.application.dto.ProfessionalAdminDto;

import java.util.List;

/**
 * Endpoints de administración para moderación de profesionales.
 * Protegido por SecurityConfig: /api/v1/admin/** requiere ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/v1/admin/professionals")
public class AdminProfessionalController {

    private final AdminProfessionalService adminProfessionalService;

    public AdminProfessionalController(AdminProfessionalService adminProfessionalService) {
        this.adminProfessionalService = adminProfessionalService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ProfessionalAdminDto>> getPending() {
        return ResponseEntity.ok(adminProfessionalService.findPending());
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ProfessionalAdminDto> approve(@PathVariable Long id) {
        return ResponseEntity.ok(adminProfessionalService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ProfessionalAdminDto> reject(@PathVariable Long id) {
        return ResponseEntity.ok(adminProfessionalService.reject(id));
    }
}
