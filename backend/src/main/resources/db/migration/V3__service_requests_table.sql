-- ServiPy - Flyway Migration V3
-- Solicitudes de Servicio: tabla principal

CREATE TABLE service_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professional_id BIGINT NOT NULL,
    client_name VARCHAR(150) NOT NULL,
    client_email VARCHAR(255) NOT NULL,
    client_phone VARCHAR(20),
    subject VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    desired_date DATE,
    status ENUM('PENDING','ACCEPTED','REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_request_professional FOREIGN KEY (professional_id)
        REFERENCES professional_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Índices para consultas frecuentes
CREATE INDEX idx_requests_professional ON service_requests(professional_id);
CREATE INDEX idx_requests_status ON service_requests(status);
CREATE INDEX idx_requests_professional_status ON service_requests(professional_id, status);
CREATE INDEX idx_requests_created_at ON service_requests(created_at);
