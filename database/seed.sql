-- ServiPy - Seed Data para desarrollo local
-- Contraseña para todos los usuarios: password123
-- BCrypt hash generado con strength 10
-- NO incluir en producción.

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================================
-- NOTA: Este script debe ejecutarse DESPUÉS de que Flyway
-- haya creado las tablas (es decir, después de que el backend
-- arranque por primera vez).
-- Ejecutar manualmente:
--   mysql -u servipy_user -pservipy_pass servipy < database/seed.sql
-- ============================================================

-- Países
INSERT INTO countries (id, name, code, default_currency) VALUES
(1, 'Paraguay', 'PY', 'PYG'),
(2, 'Argentina', 'AR', 'ARS'),
(3, 'Brasil', 'BR', 'BRL');

-- Ciudades de Paraguay
INSERT INTO cities (id, country_id, name, latitude, longitude) VALUES
(1, 1, 'Asunción', -25.2637, -57.5759),
(2, 1, 'Ciudad del Este', -25.5097, -54.6110),
(3, 1, 'Encarnación', -27.3307, -55.8667),
(4, 1, 'San Lorenzo', -25.3389, -57.5094),
(5, 1, 'Luque', -25.2700, -57.4870);

-- Ciudades de Argentina
INSERT INTO cities (id, country_id, name, latitude, longitude) VALUES
(6, 2, 'Buenos Aires', -34.6037, -58.3816),
(7, 2, 'Córdoba', -31.4201, -64.1888);

-- Categorías de servicio
INSERT INTO categories (id, name, icon, description, active) VALUES
(1, 'Plomería', 'wrench', 'Instalación y reparación de sistemas de agua y gas', TRUE),
(2, 'Electricidad', 'zap', 'Instalaciones eléctricas, reparaciones y mantenimiento', TRUE),
(3, 'Limpieza', 'sparkles', 'Limpieza de hogar, oficinas y espacios comerciales', TRUE),
(4, 'Pintura', 'paintbrush', 'Pintura interior y exterior, acabados decorativos', TRUE),
(5, 'Jardinería', 'flower', 'Mantenimiento de jardines, poda y diseño paisajístico', TRUE),
(6, 'Carpintería', 'hammer', 'Muebles a medida, reparaciones y restauraciones', TRUE),
(7, 'Cerrajería', 'key', 'Apertura de puertas, cambio de cerraduras, copias de llaves', TRUE),
(8, 'Mudanzas', 'truck', 'Transporte de muebles y pertenencias', TRUE),
(9, 'Aire Acondicionado', 'thermometer', 'Instalación, limpieza y reparación de equipos de climatización', TRUE),
(10, 'Albañilería', 'brick', 'Construcción, reformas y reparaciones generales', TRUE);

-- Usuarios (password: password123)
-- BCrypt hash generado por Spring Security BCryptPasswordEncoder (strength 10)
INSERT INTO users (id, name, email, password_hash, role, active) VALUES
-- Clientes
(1, 'Carlos Méndez', 'carlos@example.com', '$2a$10$jYGX5EwQ/6u/1UZ9H1PQMur3HPB1rFuBSz2.ZmGBCi4mmxBWzuo/a', 'CLIENT', TRUE),
(2, 'María González', 'maria@example.com', '$2a$10$jYGX5EwQ/6u/1UZ9H1PQMur3HPB1rFuBSz2.ZmGBCi4mmxBWzuo/a', 'CLIENT', TRUE),
(3, 'Juan Pérez', 'juan@example.com', '$2a$10$jYGX5EwQ/6u/1UZ9H1PQMur3HPB1rFuBSz2.ZmGBCi4mmxBWzuo/a', 'CLIENT', TRUE),
-- Profesionales
(4, 'Roberto Silva', 'roberto@example.com', '$2a$10$jYGX5EwQ/6u/1UZ9H1PQMur3HPB1rFuBSz2.ZmGBCi4mmxBWzuo/a', 'PROFESSIONAL', TRUE),
(5, 'Ana Ramírez', 'ana@example.com', '$2a$10$jYGX5EwQ/6u/1UZ9H1PQMur3HPB1rFuBSz2.ZmGBCi4mmxBWzuo/a', 'PROFESSIONAL', TRUE),
(6, 'Pedro Giménez', 'pedro@example.com', '$2a$10$jYGX5EwQ/6u/1UZ9H1PQMur3HPB1rFuBSz2.ZmGBCi4mmxBWzuo/a', 'PROFESSIONAL', TRUE),
(7, 'Laura Benítez', 'laura@example.com', '$2a$10$jYGX5EwQ/6u/1UZ9H1PQMur3HPB1rFuBSz2.ZmGBCi4mmxBWzuo/a', 'PROFESSIONAL', TRUE),
(8, 'Diego Fernández', 'diego@example.com', '$2a$10$jYGX5EwQ/6u/1UZ9H1PQMur3HPB1rFuBSz2.ZmGBCi4mmxBWzuo/a', 'PROFESSIONAL', TRUE),
-- Admin
(9, 'Admin ServiPy', 'admin@servipy.com', '$2a$10$jYGX5EwQ/6u/1UZ9H1PQMur3HPB1rFuBSz2.ZmGBCi4mmxBWzuo/a', 'ADMIN', TRUE);

-- Perfiles profesionales
INSERT INTO professional_profiles (id, user_id, photo_url, phone, whatsapp, description, city_id, approval_status, availability) VALUES
(1, 4, 'https://i.pravatar.cc/300?u=roberto', '+595981123456', '+595981123456',
 'Plomero con 15 años de experiencia. Especialista en instalaciones sanitarias y reparaciones de emergencia. Trabajo garantizado.',
 1, 'APPROVED', 'PRESENCIAL'),
(2, 5, NULL, '+595982234567', '+595982234567',
 'Electricista matriculada. Instalaciones domiciliarias e industriales. Certificada en normas NEC.',
 1, 'APPROVED', 'PRESENCIAL'),
(3, 6, 'https://i.pravatar.cc/300?u=pedro', '+595983345678', '+595983345678',
 'Servicio de limpieza profesional para hogares y oficinas. Productos ecológicos. Equipo propio.',
 2, 'APPROVED', 'PRESENCIAL'),
(4, 7, 'https://i.pravatar.cc/300?u=laura', '+595984456789', '+595984456789',
 'Pintora profesional con especialización en acabados decorativos y técnicas modernas. Presupuesto sin cargo.',
 1, 'APPROVED', 'AMBOS'),
(5, 8, NULL, '+595985567890', '+595985567890',
 'Jardinero paisajista. Diseño, mantenimiento y poda artística. Consultas virtuales disponibles.',
 4, 'PENDING', 'AMBOS');

-- Servicios ofrecidos
INSERT INTO offered_services (id, professional_id, category_id, name, description, price, currency, active) VALUES
-- Roberto (Plomero)
(1, 1, 1, 'Reparación de cañerías', 'Reparación de pérdidas, cambio de cañerías deterioradas', 150000, 'PYG', TRUE),
(2, 1, 1, 'Instalación de grifo', 'Instalación de grifos y canillas nuevas', 100000, 'PYG', TRUE),
(3, 1, 1, 'Destape de desagüe', 'Desobstrucción de cañerías y desagües', 120000, 'PYG', TRUE),
-- Ana (Electricista)
(4, 2, 2, 'Instalación eléctrica completa', 'Cableado y tablero para viviendas nuevas', 2500000, 'PYG', TRUE),
(5, 2, 2, 'Reparación de cortocircuitos', 'Diagnóstico y reparación de fallas eléctricas', 200000, 'PYG', TRUE),
(6, 2, 2, 'Instalación de luminarias', 'Colocación de lámparas, spots y apliques', 80000, 'PYG', TRUE),
-- Pedro (Limpieza)
(7, 3, 3, 'Limpieza de hogar estándar', 'Limpieza completa de casa hasta 3 habitaciones', 250000, 'PYG', TRUE),
(8, 3, 3, 'Limpieza profunda', 'Limpieza exhaustiva incluyendo muebles y alfombras', 450000, 'PYG', TRUE),
-- Laura (Pintora)
(9, 4, 4, 'Pintura de habitación', 'Pintura interior de una habitación estándar (hasta 20m²)', 350000, 'PYG', TRUE),
(10, 4, 4, 'Pintura exterior', 'Pintura de fachadas y muros exteriores', 800000, 'PYG', TRUE),
-- Diego (Jardinero - PENDING, así probamos que no aparece en catálogo)
(11, 5, 5, 'Mantenimiento de jardín', 'Corte de césped, poda y limpieza quincenal', 180000, 'PYG', TRUE),
(12, 5, 5, 'Diseño paisajístico', 'Diseño completo de jardín con plano y presupuesto', 500000, 'PYG', TRUE);

-- Solicitudes de servicio (service_requests)
INSERT INTO service_requests (id, professional_id, client_name, client_email, client_phone, subject, description, desired_date, status) VALUES
(1, 1, 'Carlos Méndez', 'carlos@example.com', '+595991111111',
 'Pérdida de agua en cocina', 'Tengo una pérdida de agua debajo de la pileta de la cocina. Parece ser una junta deteriorada.',
 '2026-08-01', 'PENDING'),
(2, 1, 'María González', 'maria@example.com', '+595992222222',
 'Instalación de grifo nuevo', 'Necesito cambiar el grifo del baño principal. Ya tengo el repuesto comprado.',
 '2026-08-05', 'ACCEPTED'),
(3, 2, 'Carlos Méndez', 'carlos@example.com', '+595991111111',
 'Problema con disyuntor', 'El disyuntor del tablero salta cada vez que enciendo el aire acondicionado.',
 '2026-07-30', 'PENDING'),
(4, 4, 'Juan Pérez', 'juan@example.com', '+595993333333',
 'Pintura de living', 'Quiero pintar el living-comedor de mi departamento. Son aprox 35m².',
 '2026-08-10', 'REJECTED');
