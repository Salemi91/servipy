package py.com.servipy.professional.infrastructure.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import py.com.servipy.professional.application.OfferedServiceService;
import py.com.servipy.professional.application.dto.CreateOfferedServiceRequest;
import py.com.servipy.professional.application.dto.OfferedServiceDto;
import py.com.servipy.user.domain.User;

import java.util.List;

/**
 * Tarifario del profesional autenticado.
 */
@RestController
@RequestMapping("/api/v1/professional/profile/services")
@PreAuthorize("hasRole('PROFESSIONAL')")
public class ProfessionalOfferedServiceController {

    private final OfferedServiceService offeredServiceService;

    public ProfessionalOfferedServiceController(OfferedServiceService offeredServiceService) {
        this.offeredServiceService = offeredServiceService;
    }

    @GetMapping
    public ResponseEntity<List<OfferedServiceDto>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(offeredServiceService.findOwnServices(user.getId()));
    }

    @PostMapping
    public ResponseEntity<OfferedServiceDto> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateOfferedServiceRequest request) {
        OfferedServiceDto created = offeredServiceService.create(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long serviceId) {
        offeredServiceService.deactivate(user.getId(), serviceId);
        return ResponseEntity.noContent().build();
    }
}
