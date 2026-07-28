package py.com.servipy.client.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import py.com.servipy.client.application.ClientRequestService;
import py.com.servipy.client.application.dto.ServiceRequestDetailResponse;
import py.com.servipy.client.application.dto.ServiceRequestPageResponse;
import py.com.servipy.user.domain.User;

/**
 * Controlador REST para el historial de solicitudes del cliente.
 * Requiere rol CLIENT para acceder a todos los endpoints.
 */
@RestController
@RequestMapping("/api/v1/client/requests")
@PreAuthorize("hasRole('CLIENT')")
public class ClientRequestController {

    private final ClientRequestService clientRequestService;

    public ClientRequestController(ClientRequestService clientRequestService) {
        this.clientRequestService = clientRequestService;
    }

    /**
     * GET /api/v1/client/requests?status=&page=0&size=20
     * Retorna el historial paginado de solicitudes del cliente autenticado.
     */
    @GetMapping
    public ResponseEntity<ServiceRequestPageResponse> getRequests(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (page < 0) {
            throw new IllegalArgumentException("El parámetro 'page' debe ser mayor o igual a 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("El parámetro 'size' debe estar entre 1 y 100");
        }

        ServiceRequestPageResponse response = clientRequestService.getRequests(user.getEmail(), status, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/client/requests/{id}
     * Retorna el detalle de una solicitud específica del cliente autenticado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequestDetailResponse> getRequestDetail(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        ServiceRequestDetailResponse response = clientRequestService.getRequestDetail(id, user.getEmail());
        return ResponseEntity.ok(response);
    }
}
