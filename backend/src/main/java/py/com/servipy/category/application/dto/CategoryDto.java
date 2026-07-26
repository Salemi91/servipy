package py.com.servipy.category.application.dto;

public record CategoryDto(
        Long id,
        String name,
        String icon,
        String description
) {
}
