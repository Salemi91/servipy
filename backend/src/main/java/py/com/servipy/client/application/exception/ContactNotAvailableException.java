package py.com.servipy.client.application.exception;

/**
 * Se lanza cuando el cliente solicita los datos de contacto del profesional
 * y la solicitud todavía no fue aceptada.
 */
public class ContactNotAvailableException extends RuntimeException {

    public ContactNotAvailableException(String message) {
        super(message);
    }
}
