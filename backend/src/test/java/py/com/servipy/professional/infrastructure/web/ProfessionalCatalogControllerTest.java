package py.com.servipy.professional.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import py.com.servipy.auth.application.JwtService;
import py.com.servipy.category.application.CategoryService;
import py.com.servipy.category.application.dto.CategoryDto;
import py.com.servipy.category.infrastructure.web.CategoryController;
import py.com.servipy.professional.application.ProfessionalCatalogService;
import py.com.servipy.professional.application.dto.ProfessionalDetailDto;
import py.com.servipy.professional.application.dto.ProfessionalSummaryDto;
import py.com.servipy.shared.config.SecurityConfig;
import py.com.servipy.shared.exception.ResourceNotFoundException;
import py.com.servipy.shared.web.ErrorResponseWriter;
import py.com.servipy.shared.web.RestAccessDeniedHandler;
import py.com.servipy.shared.web.RestAuthenticationEntryPoint;
import py.com.servipy.user.infrastructure.persistence.UserRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ProfessionalCatalogController.class, CategoryController.class})
@Import({SecurityConfig.class, ErrorResponseWriter.class,
         RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
@ActiveProfiles("test")
class ProfessionalCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfessionalCatalogService catalogService;

    @MockBean
    private CategoryService categoryService;

    // Colaboradores del filtro JWT importado con SecurityConfig
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void should_return200_when_getProfessionalsWithoutAuth() throws Exception {
        ProfessionalSummaryDto summary = new ProfessionalSummaryDto(
                1L, "Juan Pérez", "Electricista", "Electricidad",
                "Profesional con experiencia", "Asunción",
                new BigDecimal("150000"), "PRESENCIAL", null
        );
        Page<ProfessionalSummaryDto> page = new PageImpl<>(List.of(summary));
        when(catalogService.findAll(isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/professionals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Juan Pérez"));
    }

    @Test
    void should_return200_when_getCategoriesWithoutAuth() throws Exception {
        CategoryDto cat = new CategoryDto(1L, "Electricidad", "bolt", "Servicios eléctricos");
        when(categoryService.findAllActive()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electricidad"));
    }

    @Test
    void should_return400_when_categoryIdIsNonNumeric() throws Exception {
        mockMvc.perform(get("/api/v1/professionals").param("categoryId", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void should_return400_when_professionalIdIsNonNumeric() throws Exception {
        mockMvc.perform(get("/api/v1/professionals/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void should_omitContactData_when_getProfessionalDetailPublicly() throws Exception {
        ProfessionalDetailDto detail = new ProfessionalDetailDto(
                1L, "Juan Pérez", null, "Profesional con experiencia",
                "Asunción", "PRESENCIAL", List.of()
        );
        when(catalogService.findById(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/professionals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Juan Pérez"))
                .andExpect(jsonPath("$.phone").doesNotExist())
                .andExpect(jsonPath("$.whatsapp").doesNotExist());
    }

    @Test
    void should_return404_when_professionalIdNotFound() throws Exception {
        when(catalogService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Profesional no encontrado"));

        mockMvc.perform(get("/api/v1/professionals/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Profesional no encontrado"));
    }
}
