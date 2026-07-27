-- ServiPy - Flyway Migration V4
-- Perfil de Cliente: agregar campos phone y photo_url a users

ALTER TABLE users ADD COLUMN phone VARCHAR(20) NULL;
ALTER TABLE users ADD COLUMN photo_url VARCHAR(500) NULL;
