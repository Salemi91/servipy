package py.com.servipy.city.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import py.com.servipy.city.application.CityService;
import py.com.servipy.city.application.dto.CityDto;

import java.util.List;

/**
 * Catálogo de ciudades. Público: alimenta los filtros de búsqueda
 * y el formulario de onboarding del profesional.
 */
@RestController
@RequestMapping("/api/v1/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping
    public ResponseEntity<List<CityDto>> list() {
        return ResponseEntity.ok(cityService.findAll());
    }
}
