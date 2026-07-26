package py.com.servipy.servicerequest.application.dto;

import jakarta.validation.constraints.NotNull;

import py.com.servipy.servicerequest.domain.RequestStatus;

public record ChangeStatusDto(
    @NotNull(message = "El estado destino es obligatorio")
    RequestStatus status
) {}
