package py.com.servipy.shared.exception;

/**
 * Excepción lanzada cuando se intenta registrar un email que ya existe.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("El email '" + email + "' ya está registrado");
    }
}
