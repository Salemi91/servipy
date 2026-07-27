package py.com.servipy.category.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
    @NotBlank(message = "El nombre es obligatorio")
    String name,

    @Size(max = 50, message = "El icono no puede superar 50 caracteres")
    String icon,

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    String description
) {}
