-- ServiPy - Database Schema (Docker init)
-- Este archivo se ejecuta al crear el contenedor MySQL por primera vez.
-- Solo asegura que la BD tenga el charset correcto.
-- Flyway se encarga de crear las tablas cuando Spring Boot arranque.

ALTER DATABASE servipy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
