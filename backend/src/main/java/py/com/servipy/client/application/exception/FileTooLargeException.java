package py.com.servipy.client.application.exception;

/**
 * Excepción lanzada cuando el archivo excede el tamaño máximo permitido (5 MB).
 */
public class FileTooLargeException extends RuntimeException {

    public FileTooLargeException(String message) {
        super(message);
    }
}
