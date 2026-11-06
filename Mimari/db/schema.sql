-- MIMARI: base de datos programacion3 (MySQL o MariaDB)
-- Con Main + createDatabaseIfNotExist, Hibernate (hbm2ddl.auto=update) crea y actualiza las tablas de JPA solo.
-- Este script sirve para crearlas a mano y para ver su estructura.
CREATE DATABASE IF NOT EXISTS programacion3 CHARACTER SET utf8mb4;
USE programacion3;

-- ============================================================================
-- Semana 11: tabla de la práctica JDBC (ProductosServlet -> /productos.jsp)
-- ============================================================================
DROP TABLE IF EXISTS productos;
CREATE TABLE productos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  precio DECIMAL(10,2) NOT NULL
);
INSERT INTO productos(nombre, precio) VALUES
('Cemento gris 50 kg', 32000),
('Varilla corrugada 1/2" x 6 m', 38500),
('Ladrillo estructural', 1800),
('Arena de rio (m3)', 95000);
-- Verificación:  SELECT * FROM productos;

-- ============================================================================
-- Semana 13: tablas de las entidades JPA (com.mimari.modelo). Todo lo de una persona lleva su usuario_id.
-- OJO: si ya tenías una tabla insumo de una versión anterior (sin usuario_id), bórrala primero:  DROP TABLE insumo;
-- ============================================================================
CREATE TABLE IF NOT EXISTS usuario (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(30) NOT NULL UNIQUE,
  clave_hash VARCHAR(200) NOT NULL,            -- PBKDF2; nunca se guarda la clave
  creado DATETIME(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS insumo (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  tipo VARCHAR(60) NOT NULL,
  precio DECIMAL(14,2) NOT NULL,
  cantidad INT NOT NULL,
  creado DATETIME(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS archivo (           -- fotos del terreno (tipo "plano") y PDF de catálogo (tipo "catalogo")
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  nombre_original VARCHAR(150) NOT NULL,
  nombre_almacenado VARCHAR(80) NOT NULL,
  content_type VARCHAR(60) NOT NULL,
  tamano BIGINT NOT NULL,
  creado DATETIME(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS analisis (          -- historial de análisis de la IA
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  archivo_id BIGINT NULL,
  ancho DOUBLE NOT NULL,
  largo DOUBLE NOT NULL,
  pisos INT NOT NULL,
  altura_piso DOUBLE NOT NULL,
  motor VARCHAR(60) NOT NULL,
  resultado LONGTEXT NOT NULL,
  creado DATETIME(6) NOT NULL
);

-- Verificación:  SELECT id, nombre FROM usuario;   SELECT id, usuario_id, nombre, precio FROM insumo;
