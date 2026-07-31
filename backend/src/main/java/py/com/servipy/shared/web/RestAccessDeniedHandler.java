package py.com.servipy.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Responde 403 con la estructura de error uniforme cuando el usuario
 * está autenticado pero su rol no alcanza para el recurso.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ErrorResponseWriter errorResponseWriter;

    public RestAccessDeniedHandler(ErrorResponseWriter errorResponseWriter) {
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        errorResponseWriter.write(response, HttpStatus.FORBIDDEN, "FORBIDDEN",
            "No tiene permisos para acceder a este recurso");
    }
}
