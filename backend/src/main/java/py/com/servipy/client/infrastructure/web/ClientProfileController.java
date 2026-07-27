package py.com.servipy.client.infrastructure.web;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import py.com.servipy.client.application.ClientProfileService;
import py.com.servipy.client.application.dto.ClientProfileResponse;
import py.com.servipy.client.application.dto.ClientProfileUpdateRequest;
import py.com.servipy.client.application.dto.PasswordChangeRequest;
import py.com.servipy.client.application.dto.PhotoUploadResponse;
import py.com.servipy.client.application.exception.FileTooLargeException;

/**
 * Controlador REST para la gestión del perfil del cliente.
 * Requiere rol CLIENT para acceder a todos los endpoints.
 */
@RestController
@RequestMapping("/api/v1/client/profile")
@PreAuthorize("hasRole('CLIENT')")
public class ClientProfileController {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5 MB

    private final ClientProfileService clientProfileService;

    public ClientProfileController(ClientProfileService clientProfileService) {
        this.clientProfileService = clientProfileService;
    }

    /**
     * GET /api/v1/client/profile
     * Retorna el perfil del cliente autenticado.
     */
    @GetMapping
    public ResponseEntity<ClientProfileResponse> getProfile(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        ClientProfileResponse profile = clientProfileService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/v1/client/profile
     * Actualiza los datos personales del cliente (name y phone).
     */
    @PutMapping
    public ResponseEntity<ClientProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ClientProfileUpdateRequest request) {
        Long userId = Long.parseLong(authentication.getName());
        ClientProfileResponse profile = clientProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/v1/client/profile/photo
     * Sube una nueva foto de perfil para el cliente.
     */
    @PutMapping("/photo")
    public ResponseEntity<PhotoUploadResponse> uploadPhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        Long userId = Long.parseLong(authentication.getName());

        if (file.isEmpty() || file.getSize() == 0) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileTooLargeException("El tamaño máximo permitido es 5 MB");
        }

        PhotoUploadResponse response = clientProfileService.uploadPhoto(userId, file);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/client/profile/password
     * Cambia la contraseña del cliente autenticado.
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordChangeRequest request) {
        Long userId = Long.parseLong(authentication.getName());
        clientProfileService.changePassword(userId, request);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
    }
}
