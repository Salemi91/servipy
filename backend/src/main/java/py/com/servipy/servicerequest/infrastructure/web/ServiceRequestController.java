package py.com.servipy.servicerequest.infrastructure.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import java.util.List;

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
    public ResponseEntity<List<ServiceRequestSummaryDto>> list(
            @PathVariable Long professionalId,
            @RequestParam(required = false) RequestStatus status) {
        List<ServiceRequestSummaryDto> list = service.findByProfessional(professionalId, status);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ServiceRequestDetailDto> detail(
            @PathVariable Long professionalId,
            @PathVariable Long requestId) {
        ServiceRequestDetailDto detail = service.findDetail(professionalId, requestId);
        return ResponseEntity.ok(detail);
    }

    @PatchMapping("/{requestId}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long professionalId,
            @PathVariable Long requestId,
            @Valid @RequestBody ChangeStatusDto dto) {
        service.changeStatus(professionalId, requestId, dto.status());
        return ResponseEntity.ok().build();
    }
}
