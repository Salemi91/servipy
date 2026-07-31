package py.com.servipy.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Responde 401 con la estructura de error uniforme cuando se accede
 * a un recurso protegido sin autenticación.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponseWriter errorResponseWriter;

    public RestAuthenticationEntryPoint(ErrorResponseWriter errorResponseWriter) {
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        errorResponseWriter.write(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "No autorizado");
    }
}
