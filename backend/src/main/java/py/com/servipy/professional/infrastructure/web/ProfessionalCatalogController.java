package py.com.servipy.professional.infrastructure.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import py.com.servipy.professional.application.ProfessionalCatalogService;
import py.com.servipy.professional.application.dto.ProfessionalDetailDto;
import py.com.servipy.professional.application.dto.ProfessionalSummaryDto;

@RestController
@RequestMapping("/api/v1/professionals")
public class ProfessionalCatalogController {

    private final ProfessionalCatalogService catalogService;

    public ProfessionalCatalogController(ProfessionalCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * GET /api/v1/professionals?categoryId=&search=&page=0&size=12
     * Público — sin autenticación.
     */
    @GetMapping
    public ResponseEntity<Page<ProfessionalSummaryDto>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        int cappedSize = Math.min(size < 1 ? 12 : size, 50);
        Pageable pageable = PageRequest.of(page, cappedSize, Sort.by("user.name").ascending());

        Page<ProfessionalSummaryDto> result = catalogService.findAll(categoryId, search, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/professionals/{id}
     * Público — sin autenticación.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalDetailDto> detail(@PathVariable Long id) {
        ProfessionalDetailDto dto = catalogService.findById(id);
        return ResponseEntity.ok(dto);
    }
}
