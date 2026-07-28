package py.com.servipy.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import py.com.servipy.client.application.exception.FileTooLargeException;
import py.com.servipy.client.application.exception.InvalidCurrentPasswordException;
import py.com.servipy.client.application.exception.InvalidFileTypeException;
import py.com.servipy.client.application.exception.PhotoStorageException;

import java.time.Instant;
import java.util.List;

/**
 * Manejador global de excepciones.
 * Transforma todas las excepciones en la estructura ErrorResponse uniforme.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ErrorResponse response = new ErrorResponse(
            Instant.now().toString(),
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_ERROR",
            "Error de validación en los datos enviados",
            fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse response = buildError(
            HttpStatus.NOT_FOUND,
            "NOT_FOUND",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
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

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStateTransitionException ex) {
        ErrorResponse response = new ErrorResponse(
            Instant.now().toString(),
            HttpStatus.CONFLICT.value(),
            "INVALID_STATE_TRANSITION",
            ex.getMessage(),
            List.of()
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

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse response = buildError(
            HttpStatus.UNAUTHORIZED,
            "INVALID_CREDENTIALS",
            "Email o contraseña incorrectos"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorResponse response = buildError(
            HttpStatus.BAD_REQUEST,
            "INVALID_PARAMETER",
            "El parámetro '" + ex.getName() + "' no es válido"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileType(InvalidFileTypeException ex) {
        ErrorResponse response = buildError(
            HttpStatus.BAD_REQUEST,
            "INVALID_FILE_TYPE",
            "Tipos permitidos: image/jpeg, image/png, image/webp"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<ErrorResponse> handleFileTooLarge(FileTooLargeException ex) {
        ErrorResponse response = buildError(
            HttpStatus.BAD_REQUEST,
            "FILE_TOO_LARGE",
            "El tamaño máximo permitido es 5 MB"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(PhotoStorageException.class)
    public ResponseEntity<ErrorResponse> handlePhotoStorage(PhotoStorageException ex) {
        ErrorResponse response = buildError(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR",
            "No se pudo almacenar la imagen"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCurrentPassword(InvalidCurrentPasswordException ex) {
        ErrorResponse response = buildError(
            HttpStatus.BAD_REQUEST,
            "INVALID_CURRENT_PASSWORD",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        ErrorResponse response = buildError(
            HttpStatus.BAD_REQUEST,
            "FILE_TOO_LARGE",
            "El tamaño máximo permitido es 5 MB"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingPart(MissingServletRequestPartException ex) {
        ErrorResponse response = buildError(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "El archivo es requerido"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse response = buildError(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
