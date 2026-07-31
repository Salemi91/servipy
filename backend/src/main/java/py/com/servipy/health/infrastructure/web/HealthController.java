package py.com.servipy.health.infrastructure.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint de verificación de salud del backend.
 * Accesible sin autenticación.
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final String applicationName;

    public HealthController(@Value("${spring.application.name}") String applicationName) {
        this.applicationName = applicationName;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "application", applicationName
        ));
    }
}
