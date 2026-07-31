package py.com.servipy.servicerequest.application.dto;

import py.com.servipy.servicerequest.domain.RequestStatus;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Vista de una solicitud desde la perspectiva del cliente que la envió.
 * Es el modelo de lectura que el slice servicerequest expone al slice client.
 */
public record ClientRequestView(
    Long id,
    String subject,
    String description,
    LocalDate desiredDate,
    RequestStatus status,
    String professionalName,
    String professionalPhone,
    String professionalWhatsapp,
    Instant createdAt,
    Instant updatedAt
) {}
