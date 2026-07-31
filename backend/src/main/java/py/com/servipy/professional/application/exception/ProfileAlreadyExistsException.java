package py.com.servipy.professional.application.exception;

/**
 * Se lanza al intentar crear un segundo perfil profesional para el mismo usuario.
 */
public class ProfileAlreadyExistsException extends RuntimeException {

    public ProfileAlreadyExistsException(String message) {
        super(message);
    }
}
