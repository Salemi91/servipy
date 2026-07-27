package py.com.servipy.client.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de cambio de contraseña.
 * Valida que ambos campos estén presentes y que newPassword cumpla
 * los requisitos de longitud y complejidad (mayúscula + minúscula + dígito).
 */
public record PasswordChangeRequest(

    @NotBlank(message = "La contraseña actual es obligatoria")
    String currentPassword,

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La nueva contraseña debe tener entre 8 y 72 caracteres")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
        message = "La nueva contraseña debe contener al menos una mayúscula, una minúscula y un dígito"
    )
    String newPassword
) {}
