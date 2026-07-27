package py.com.servipy.client.infrastructure.web;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import py.com.servipy.user.domain.User;

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
    public ResponseEntity<ClientProfileResponse> getProfile(@AuthenticationPrincipal User user) {
        ClientProfileResponse profile = clientProfileService.getProfile(user.getId());
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/v1/client/profile
     * Actualiza los datos personales del cliente (name y phone).
     */
    @PutMapping
    public ResponseEntity<ClientProfileResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ClientProfileUpdateRequest request) {
        ClientProfileResponse profile = clientProfileService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/v1/client/profile/photo
     * Sube una nueva foto de perfil para el cliente.
     */
    @PutMapping("/photo")
    public ResponseEntity<PhotoUploadResponse> uploadPhoto(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || file.getSize() == 0) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileTooLargeException("El tamaño máximo permitido es 5 MB");
        }

        PhotoUploadResponse response = clientProfileService.uploadPhoto(user.getId(), file);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/client/profile/password
     * Cambia la contraseña del cliente autenticado.
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PasswordChangeRequest request) {
        clientProfileService.changePassword(user.getId(), request);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
    }
}
