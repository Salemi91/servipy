package py.com.servipy.shared.exception;

/**
 * Excepción lanzada cuando un usuario con cuenta inactiva intenta autenticarse.
 */
public class AccountInactiveException extends RuntimeException {

    public AccountInactiveException() {
        super("La cuenta se encuentra inactiva");
    }
}
