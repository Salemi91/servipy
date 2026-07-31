package py.com.servipy.servicerequest.infrastructure.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import py.com.servipy.servicerequest.application.ServiceRequestService;
import py.com.servipy.servicerequest.application.dto.ChangeStatusDto;
import py.com.servipy.servicerequest.application.dto.CreateServiceRequestDto;
import py.com.servipy.servicerequest.application.dto.CreateServiceRequestResponse;
import py.com.servipy.servicerequest.application.dto.ServiceRequestDetailDto;
import py.com.servipy.servicerequest.application.dto.ServiceRequestSummaryDto;
import py.com.servipy.servicerequest.domain.RequestStatus;
import py.com.servipy.user.domain.User;

import java.util.List;

/**
 * Endpoints de solicitudes de servicio.
 * La creación es pública (formulario del perfil del profesional).
 * La consulta y la resolución son privativas del profesional destinatario.
 */
@RestController
@RequestMapping("/api/v1/professionals/{professionalId}/service-requests")
public class ServiceRequestController {

    private final ServiceRequestService service;

    public ServiceRequestController(ServiceRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CreateServiceRequestResponse> create(
            @PathVariable Long professionalId,
            @Valid @RequestBody CreateServiceRequestDto dto) {
        CreateServiceRequestResponse response = service.create(professionalId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<List<ServiceRequestSummaryDto>> list(
            @PathVariable Long professionalId,
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) RequestStatus status) {
        List<ServiceRequestSummaryDto> list = service.findByProfessional(professionalId, user.getId(), status);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<ServiceRequestDetailDto> detail(
            @PathVariable Long professionalId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal User user) {
        ServiceRequestDetailDto detail = service.findDetail(professionalId, user.getId(), requestId);
        return ResponseEntity.ok(detail);
    }

    @PatchMapping("/{requestId}/status")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long professionalId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangeStatusDto dto) {
        service.changeStatus(professionalId, user.getId(), requestId, dto.status());
        return ResponseEntity.ok().build();
    }
}
