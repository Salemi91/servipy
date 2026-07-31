package py.com.servipy.servicerequest.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import py.com.servipy.auth.application.JwtService;
import py.com.servipy.user.domain.Role;
import py.com.servipy.user.domain.User;
import py.com.servipy.user.infrastructure.persistence.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el cierre de la exposición pública de las solicitudes de servicio:
 * solo la creación es anónima; listar, ver el detalle y cambiar el estado
 * exigen un profesional autenticado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServiceRequestSecurityTest {

    private static final String BASE_URL = "/api/v1/professionals/1/service-requests";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String clientToken;
    private String professionalToken;

    @BeforeEach
    void setUp() {
        clientToken = tokenFor("client-security@example.com", Role.CLIENT);
        professionalToken = tokenFor("professional-security@example.com", Role.PROFESSIONAL);
    }

    @Test
    void should_return401_when_listingWithoutToken() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void should_return401_when_readingDetailWithoutToken() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void should_return401_when_changingStatusWithoutToken() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACCEPTED\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void should_return403_when_clientListsProfessionalRequests() throws Exception {
        mockMvc.perform(get(BASE_URL).header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void should_return403_when_clientChangesStatus() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/1/status")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACCEPTED\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void should_return404_when_professionalQueriesForeignProfile() throws Exception {
        // El profesional autenticado no tiene perfil sobre el id 1 de la ruta
        mockMvc.perform(get(BASE_URL).header("Authorization", "Bearer " + professionalToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void should_allowAnonymousAccess_when_creatingRequest() throws Exception {
        // Payload inválido: un 400 demuestra que la petición llegó al controlador sin token
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private String tokenFor(String email, Role role) {
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseGet(() -> userRepository.save(new User("Test " + role.name(), email, "hash", role)));
        return jwtService.generateToken(user);
    }
}
