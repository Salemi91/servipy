package py.com.servipy.client.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para actualización de datos personales del cliente.
 * Solo expone name y phone; email, role e id son ignorados por diseño.
 *
 * El compact constructor aplica trimming a name y phone para que las validaciones
 * de @Size operen sobre el valor sin espacios iniciales/finales (Requisitos 2.2, 2.7).
 * Phone es nullable (Requisito 2.9): cuando es null, las anotaciones @Size y @Pattern se omiten.
 */
public record ClientProfileUpdateRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String name,

    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
    @Pattern(regexp = "^[\\d\\s\\-+]*$", message = "El teléfono solo puede contener dígitos, espacios, guiones y +")
    String phone
) {
    /**
     * Compact constructor que aplica trimming para que las validaciones consideren
     * el valor sin espacios en blanco iniciales o finales.
     */
    public ClientProfileUpdateRequest {
        if (name != null) {
            name = name.trim();
        }
        if (phone != null) {
            phone = phone.trim();
        }
    }
}
