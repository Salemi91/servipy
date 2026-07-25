package py.com.servipy.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;

/**
 * Manejador global de excepciones.
 * Transforma todas las excepciones en la estructura ErrorResponse uniforme.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
            .toList();

        ErrorResponse response = new ErrorResponse(
            Instant.now().toString(),
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_ERROR",
            "Error de validación en los campos enviados",
            fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex) {
        ErrorResponse response = buildError(
            HttpStatus.CONFLICT,
            "DUPLICATE_EMAIL",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<ErrorResponse> handleAccountInactive(AccountInactiveException ex) {
        ErrorResponse response = buildError(
            HttpStatus.FORBIDDEN,
            "ACCOUNT_INACTIVE",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse response = buildError(
            HttpStatus.FORBIDDEN,
            "FORBIDDEN",
            "No tiene permisos para acceder a este recurso"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        ErrorResponse response = buildError(
            HttpStatus.UNAUTHORIZED,
            "UNAUTHORIZED",
            "No autorizado"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        ErrorResponse response = buildError(
            HttpStatus.METHOD_NOT_ALLOWED,
            "METHOD_NOT_ALLOWED",
            "El método HTTP no está permitido para este recurso"
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedRequest(HttpMessageNotReadableException ex) {
        ErrorResponse response = buildError(
            HttpStatus.BAD_REQUEST,
            "MALFORMED_REQUEST",
            "El cuerpo de la solicitud no es válido"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(NoResourceFoundException ex) {
        ErrorResponse response = buildError(
            HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND",
            "El recurso solicitado no existe"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorResponse response = buildError(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR",
            "Ha ocurrido un error interno del servidor"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ErrorResponse buildError(HttpStatus status, String code, String message) {
        return new ErrorResponse(
            Instant.now().toString(),
            status.value(),
            code,
            message,
            List.of()
        );
    }
}
