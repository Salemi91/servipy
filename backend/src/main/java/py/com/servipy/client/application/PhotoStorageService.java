package py.com.servipy.client.application;

import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio de almacenamiento de fotos de perfil.
 */
public interface PhotoStorageService {

    /**
     * Almacena una imagen de perfil para el usuario indicado.
     * Valida el contenido real del archivo (magic bytes) para asegurar que es JPEG, PNG o WebP.
     *
     * @param userId identificador del usuario
     * @param file archivo multipart a almacenar
     * @return URL pública de acceso a la imagen almacenada
     * @throws py.com.servipy.client.application.exception.InvalidFileTypeException si los magic bytes no corresponden a un tipo permitido
     * @throws py.com.servipy.client.application.exception.PhotoStorageException si ocurre un error de IO durante el almacenamiento
     */
    String store(Long userId, MultipartFile file);
}
