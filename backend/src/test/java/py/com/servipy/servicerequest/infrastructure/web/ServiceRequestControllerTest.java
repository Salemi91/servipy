package py.com.servipy.servicerequest.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import py.com.servipy.servicerequest.application.ServiceRequestService;
import py.com.servipy.servicerequest.application.dto.CreateServiceRequestResponse;
import py.com.servipy.shared.exception.GlobalExceptionHandler;
import py.com.servipy.shared.exception.InvalidStateTransitionException;
import py.com.servipy.shared.exception.ResourceNotFoundException;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ServiceRequestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ServiceRequestService service;

    @InjectMocks
    private ServiceRequestController controller;

    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/professionals/1/service-requests";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // --- 6.2 Probar campos inválidos ---

    @Test
    void should_return400_when_nameMissing() throws Exception {
        // Arrange
        Map<String, Object> payload = buildValidPayload();
        payload.remove("name");

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void should_return400_when_emailInvalid() throws Exception {
        // Arrange
        Map<String, Object> payload = buildValidPayload();
        payload.put("email", "not-an-email");

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @Test
    void should_return400_when_descriptionTooLong() throws Exception {
        // Arrange
        Map<String, Object> payload = buildValidPayload();
        payload.put("description", "x".repeat(2001));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors[0].field").value("description"));
    }

    // --- Additional controller tests for HTTP status coverage ---

    @Test
    void should_return201_when_validPayload() throws Exception {
        // Arrange
        Map<String, Object> payload = buildValidPayload();
        when(service.create(eq(1L), any())).thenReturn(new CreateServiceRequestResponse(10L));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void should_return404_when_professionalNotExists() throws Exception {
        // Arrange
        Map<String, Object> payload = buildValidPayload();
        when(service.create(eq(1L), any()))
            .thenThrow(new ResourceNotFoundException("Profesional no encontrado"));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Profesional no encontrado"));
    }

    @Test
    void should_return409_when_invalidTransition() throws Exception {
        // Arrange
        String statusUrl = BASE_URL + "/5/status";
        Map<String, Object> statusPayload = Map.of("status", "ACCEPTED");

        doThrow(new InvalidStateTransitionException("No se puede cambiar de ACCEPTED a ACCEPTED"))
            .when(service).changeStatus(eq(1L), eq(5L), any());

        // Act & Assert
        mockMvc.perform(patch(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusPayload)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    // --- Helper methods ---

    private Map<String, Object> buildValidPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Juan Pérez");
        payload.put("email", "juan@example.com");
        payload.put("phone", "0981123456");
        payload.put("subject", "Reparación de cañería");
        payload.put("description", "Necesito reparar la cañería del baño");
        payload.put("desiredDate", "2025-02-15");
        return payload;
    }
}
