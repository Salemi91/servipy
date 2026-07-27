package py.com.servipy.client.application.exception;

/**
 * Excepción lanzada cuando el tipo MIME del archivo no es permitido.
 */
public class InvalidFileTypeException extends RuntimeException {

    public InvalidFileTypeException(String message) {
        super(message);
    }
}
