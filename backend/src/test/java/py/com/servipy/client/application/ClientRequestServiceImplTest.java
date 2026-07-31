package py.com.servipy.client.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import py.com.servipy.client.application.dto.ProfessionalContactResponse;
import py.com.servipy.client.application.dto.ServiceRequestPageResponse;
import py.com.servipy.client.application.exception.ContactNotAvailableException;
import py.com.servipy.servicerequest.application.ServiceRequestService;
import py.com.servipy.servicerequest.application.dto.ClientRequestView;
import py.com.servipy.servicerequest.domain.RequestStatus;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientRequestServiceImplTest {

    private static final String CLIENT_EMAIL = "maria@example.com";

    @Mock
    private ServiceRequestService serviceRequestService;

    @InjectMocks
    private ClientRequestServiceImpl service;

    @Test
    void should_returnContactData_when_requestIsAccepted() {
        when(serviceRequestService.findByIdAndClientEmail(5L, CLIENT_EMAIL))
            .thenReturn(buildView(RequestStatus.ACCEPTED));

        ProfessionalContactResponse contact = service.getProfessionalContact(5L, CLIENT_EMAIL);

        assertThat(contact.professionalName()).isEqualTo("Juan Pérez");
        assertThat(contact.phone()).isEqualTo("0981123456");
        assertThat(contact.whatsapp()).isEqualTo("0981123456");
    }

    @Test
    void should_throw409_when_requestIsStillPending() {
        when(serviceRequestService.findByIdAndClientEmail(5L, CLIENT_EMAIL))
            .thenReturn(buildView(RequestStatus.PENDING));

        assertThatThrownBy(() -> service.getProfessionalContact(5L, CLIENT_EMAIL))
            .isInstanceOf(ContactNotAvailableException.class);
    }

    @Test
    void should_throw409_when_requestWasRejected() {
        when(serviceRequestService.findByIdAndClientEmail(5L, CLIENT_EMAIL))
            .thenReturn(buildView(RequestStatus.REJECTED));

        assertThatThrownBy(() -> service.getProfessionalContact(5L, CLIENT_EMAIL))
            .isInstanceOf(ContactNotAvailableException.class);
    }

    @Test
    void should_listOwnRequests_when_noStatusFilterIsGiven() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<ClientRequestView> page = new PageImpl<>(List.of(buildView(RequestStatus.PENDING)), pageable, 1);
        when(serviceRequestService.findByClientEmail(eq(CLIENT_EMAIL), isNull(), any(Pageable.class)))
            .thenReturn(page);

        ServiceRequestPageResponse result = service.getRequests(CLIENT_EMAIL, null, 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).professionalName()).isEqualTo("Juan Pérez");
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void should_rejectUnknownStatus_when_filterIsInvalid() {
        assertThatThrownBy(() -> service.getRequests(CLIENT_EMAIL, "COMPLETED", 0, 20))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("PENDING, ACCEPTED, REJECTED");
    }

    private ClientRequestView buildView(RequestStatus status) {
        return new ClientRequestView(
            5L,
            "Reparación de cañería",
            "Pierde agua el baño",
            null,
            status,
            "Juan Pérez",
            "0981123456",
            "0981123456",
            Instant.now(),
            Instant.now()
        );
    }
}
