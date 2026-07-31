package py.com.servipy.professional.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateOfferedServiceRequest(

    @NotNull(message = "La categoría es obligatoria")
    Long categoryId,

    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    String name,

    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    String description,

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2, message = "El precio admite hasta 10 enteros y 2 decimales")
    BigDecimal price,

    @Size(min = 3, max = 3, message = "La moneda debe tener 3 caracteres (ej. PYG)")
    String currency
) {
    private static final String DEFAULT_CURRENCY = "PYG";

    public String currencyOrDefault() {
        return currency == null || currency.isBlank() ? DEFAULT_CURRENCY : currency.toUpperCase();
    }
}
