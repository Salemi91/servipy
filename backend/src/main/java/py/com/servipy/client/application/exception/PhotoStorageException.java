package py.com.servipy.client.application.exception;

/**
 * Excepción lanzada cuando el almacenamiento de la imagen falla por un error de IO.
 */
public class PhotoStorageException extends RuntimeException {

    public PhotoStorageException(String message) {
        super(message);
    }

    public PhotoStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
