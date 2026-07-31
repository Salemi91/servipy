package py.com.servipy.client.application.dto;

/**
 * Datos de contacto del profesional, entregados al cliente
 * únicamente cuando su solicitud fue aceptada.
 */
public record ProfessionalContactResponse(
    String professionalName,
    String phone,
    String whatsapp
) {}
