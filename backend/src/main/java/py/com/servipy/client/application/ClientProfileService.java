package py.com.servipy.client.application;

import org.springframework.web.multipart.MultipartFile;

import py.com.servipy.client.application.dto.ClientProfileResponse;
import py.com.servipy.client.application.dto.ClientProfileUpdateRequest;
import py.com.servipy.client.application.dto.PasswordChangeRequest;
import py.com.servipy.client.application.dto.PhotoUploadResponse;

/**
 * Servicio de gestión del perfil del cliente.
 */
public interface ClientProfileService {

    /**
     * Obtiene el perfil del cliente autenticado.
     *
     * @param userId identificador del usuario extraído del SecurityContext
     * @return DTO con los datos del perfil del cliente
     * @throws py.com.servipy.shared.exception.ResourceNotFoundException si el usuario no existe
     */
    ClientProfileResponse getProfile(Long userId);

    /**
     * Actualiza los datos personales del cliente (name y phone).
     * Los campos email, role e id son ignorados por diseño del DTO.
     *
     * @param userId identificador del usuario extraído del SecurityContext
     * @param request DTO con los datos a actualizar
     * @return DTO con el perfil actualizado
     * @throws py.com.servipy.shared.exception.ResourceNotFoundException si el usuario no existe
     */
    ClientProfileResponse updateProfile(Long userId, ClientProfileUpdateRequest request);

    /**
     * Sube una nueva foto de perfil para el cliente.
     * Valida el contenido del archivo y lo almacena, luego actualiza el photoUrl del usuario.
     *
     * @param userId identificador del usuario extraído del SecurityContext
     * @param file archivo de imagen multipart
     * @return DTO con la URL de la nueva foto
     * @throws py.com.servipy.shared.exception.ResourceNotFoundException si el usuario no existe
     * @throws py.com.servipy.client.application.exception.InvalidFileTypeException si el tipo de archivo no es válido
     * @throws py.com.servipy.client.application.exception.PhotoStorageException si falla el almacenamiento
     */
    PhotoUploadResponse uploadPhoto(Long userId, MultipartFile file);

    /**
     * Cambia la contraseña del cliente autenticado.
     * Verifica la contraseña actual, valida que la nueva sea diferente,
     * genera nuevo hash bcrypt y actualiza en BD.
     *
     * @param userId identificador del usuario extraído del SecurityContext
     * @param request DTO con contraseña actual y nueva
     * @throws py.com.servipy.client.application.exception.InvalidCurrentPasswordException si la contraseña actual no coincide
     * @throws IllegalArgumentException si la nueva contraseña es igual a la actual
     */
    void changePassword(Long userId, PasswordChangeRequest request);
}
