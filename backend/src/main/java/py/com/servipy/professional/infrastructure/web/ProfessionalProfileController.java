package py.com.servipy.professional.infrastructure.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import py.com.servipy.professional.application.ProfessionalProfileService;
import py.com.servipy.professional.application.dto.CreateProfessionalProfileRequest;
import py.com.servipy.professional.application.dto.ProfessionalProfileResponse;
import py.com.servipy.user.domain.User;

import java.util.Optional;

/**
 * Endpoints para que el profesional logueado gestione su propio perfil.
 */
@RestController
@RequestMapping("/api/v1/professional/profile")
@PreAuthorize("hasRole('PROFESSIONAL')")
public class ProfessionalProfileController {

    private final ProfessionalProfileService profileService;

    public ProfessionalProfileController(ProfessionalProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * GET /api/v1/professional/profile/me
     * Retorna el perfil del profesional logueado, o 404 si no tiene.
     */
    @GetMapping("/me")
    public ResponseEntity<ProfessionalProfileResponse> getMyProfile(@AuthenticationPrincipal User user) {
        Optional<ProfessionalProfileResponse> profile = profileService.findByUserId(user.getId());
        return profile
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/professional/profile
     * Crea el perfil profesional (onboarding). Status inicial: PENDING.
     */
    @PostMapping
    public ResponseEntity<ProfessionalProfileResponse> createProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateProfessionalProfileRequest request) {
        ProfessionalProfileResponse response = profileService.create(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
