package py.com.servipy.client.application.exception;

/**
 * Excepción lanzada cuando la contraseña actual proporcionada
 * no coincide con el hash almacenado.
 */
public class InvalidCurrentPasswordException extends RuntimeException {

    public InvalidCurrentPasswordException(String message) {
        super(message);
    }
}
