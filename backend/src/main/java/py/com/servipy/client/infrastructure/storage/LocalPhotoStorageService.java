package py.com.servipy.client.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import py.com.servipy.client.application.PhotoStorageService;
import py.com.servipy.client.application.exception.InvalidFileTypeException;
import py.com.servipy.client.application.exception.PhotoStorageException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Implementación de almacenamiento local de fotos de perfil.
 * Almacena archivos en un directorio configurable y retorna la URL pública de acceso.
 */
@Service
public class LocalPhotoStorageService implements PhotoStorageService {

    private final String uploadDir;

    public LocalPhotoStorageService(@Value("${servipy.upload.dir:uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public String store(Long userId, MultipartFile file) {
        String extension = detectExtensionByMagicBytes(file);
        String filename = UUID.randomUUID() + "." + extension;

        Path targetDir = Paths.get(uploadDir, "clients", userId.toString());
        Path targetPath = targetDir.resolve(filename);

        try {
            Files.createDirectories(targetDir);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new PhotoStorageException("No se pudo almacenar la imagen", e);
        }

        return "/uploads/clients/" + userId + "/" + filename;
    }

    /**
     * Detecta el tipo real del archivo leyendo sus magic bytes.
     * - JPEG: empieza con FF D8 FF
     * - PNG: empieza con 89 50 4E 47
     * - WebP: bytes 0-3 = "RIFF", bytes 8-11 = "WEBP"
     *
     * @return extensión del archivo (jpg, png, webp)
     * @throws InvalidFileTypeException si no coincide con ningún tipo permitido
     */
    private String detectExtensionByMagicBytes(MultipartFile file) {
        byte[] header;
        try (InputStream is = file.getInputStream()) {
            header = new byte[12];
            int bytesRead = is.read(header);
            if (bytesRead < 12) {
                throw new InvalidFileTypeException("Tipos permitidos: image/jpeg, image/png, image/webp");
            }
        } catch (IOException e) {
            throw new PhotoStorageException("No se pudo leer el archivo", e);
        }

        // JPEG: FF D8 FF
        if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return "jpg";
        }

        // PNG: 89 50 4E 47
        if ((header[0] & 0xFF) == 0x89 && (header[1] & 0xFF) == 0x50
                && (header[2] & 0xFF) == 0x4E && (header[3] & 0xFF) == 0x47) {
            return "png";
        }

        // WebP: bytes 0-3 = "RIFF", bytes 8-11 = "WEBP"
        if (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "webp";
        }

        throw new InvalidFileTypeException("Tipos permitidos: image/jpeg, image/png, image/webp");
    }
}
