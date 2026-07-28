package py.com.servipy.professional.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProfessionalProfileRequest(

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    String phone,

    @Size(max = 20, message = "El WhatsApp no puede exceder 20 caracteres")
    String whatsapp,

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    String description,

    @NotNull(message = "La ciudad es obligatoria")
    Long cityId,

    @NotBlank(message = "La disponibilidad es obligatoria")
    String availability
) {}
