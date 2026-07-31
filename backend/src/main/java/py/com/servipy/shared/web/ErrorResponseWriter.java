package py.com.servipy.shared.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import py.com.servipy.shared.exception.ErrorResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * Escribe respuestas de error con la estructura uniforme ErrorResponse
 * desde la cadena de filtros, donde GlobalExceptionHandler no interviene.
 */
@Component
public class ErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public ErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, HttpStatus status, String code, String message)
            throws IOException {
        ErrorResponse body = new ErrorResponse(
            Instant.now().toString(),
            status.value(),
            code,
            message,
            List.of()
        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
