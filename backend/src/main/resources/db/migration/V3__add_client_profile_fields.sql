-- ServiPy - Flyway Migration V3
-- Perfil de Cliente: agregar campos a users y crear tabla service_requests

ALTER TABLE users ADD COLUMN phone VARCHAR(20) NULL;
ALTER TABLE users ADD COLUMN photo_url VARCHAR(500) NULL;

CREATE TABLE service_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    professional_name VARCHAR(150) NOT NULL,
    status ENUM('PENDING','ACCEPTED','REJECTED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_request_client FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_request_professional FOREIGN KEY (professional_id) REFERENCES users(id),
    INDEX idx_requests_client_status (client_id, status),
    INDEX idx_requests_client_created (client_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
