package py.com.servipy.client.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import py.com.servipy.client.application.dto.ClientProfileResponse;
import py.com.servipy.client.application.dto.ClientProfileUpdateRequest;
import py.com.servipy.client.application.dto.PasswordChangeRequest;
import py.com.servipy.client.application.dto.PhotoUploadResponse;
import py.com.servipy.client.application.exception.InvalidCurrentPasswordException;
import py.com.servipy.shared.exception.ResourceNotFoundException;
import py.com.servipy.user.domain.User;
import py.com.servipy.user.infrastructure.persistence.UserRepository;


/**
 * Implementación del servicio de perfil del cliente.
 */
@Service
@Transactional(readOnly = true)
public class ClientProfileServiceImpl implements ClientProfileService {

    private final UserRepository userRepository;
    private final PhotoStorageService photoStorageService;
    private final PasswordEncoder passwordEncoder;

    public ClientProfileServiceImpl(UserRepository userRepository,
                                    PhotoStorageService photoStorageService,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.photoStorageService = photoStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ClientProfileResponse getProfile(Long userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));

        return toResponse(user);
    }

    @Override
    @Transactional
    public ClientProfileResponse updateProfile(Long userId, ClientProfileUpdateRequest request) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));

        user.setName(request.name());
        user.setPhone(request.phone());

        return toResponse(user);
    }

    @Override
    @Transactional
    public PhotoUploadResponse uploadPhoto(Long userId, MultipartFile file) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));

        String photoUrl = photoStorageService.store(userId, file);

        user.setPhotoUrl(photoUrl);

        return new PhotoUploadResponse(photoUrl);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException("La contraseña actual es incorrecta");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private ClientProfileResponse toResponse(User user) {
        return new ClientProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getPhotoUrl(),
                user.getUpdatedAt()
        );
    }
}
