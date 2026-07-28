package py.com.servipy.client.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import java.time.Instant;

/**
 * Vista de solo lectura de las solicitudes de servicio desde la perspectiva del cliente.
 * Usa @Subselect para evitar conflictos con la entidad principal ServiceRequest.
 * Incluye JOIN para obtener nombre del profesional y filtra por client_email.
 */
@Entity(name = "ClientServiceRequest")
@Immutable
@Subselect("""
    SELECT sr.id,
           sr.professional_id,
           sr.client_email,
           sr.subject AS service_name,
           u.name AS professional_name,
           sr.status,
           sr.created_at,
           sr.updated_at
    FROM service_requests sr
    JOIN professional_profiles pp ON pp.id = sr.professional_id
    JOIN users u ON u.id = pp.user_id
    """)
@Synchronize("service_requests")
public class ServiceRequest {

    @Id
    private Long id;

    @Column(name = "professional_id")
    private Long professionalId;

    @Column(name = "client_email")
    private String clientEmail;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "professional_name")
    private String professionalName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public ServiceRequest() {
    }

    public Long getId() {
        return id;
    }

    public Long getProfessionalId() {
        return professionalId;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getProfessionalName() {
        return professionalName;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
