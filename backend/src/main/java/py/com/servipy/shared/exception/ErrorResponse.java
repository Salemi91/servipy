package py.com.servipy.shared.exception;

import java.util.List;

/**
 * Estructura uniforme de error para todas las respuestas con código HTTP >= 400.
 * Alineada con el contrato definido en docs/API_CONTRACT.md.
 */
public record ErrorResponse(
    String timestamp,
    int status,
    String code,
    String message,
    List<FieldError> errors
) {

    public record FieldError(
        String field,
        String message
    ) {}
}
