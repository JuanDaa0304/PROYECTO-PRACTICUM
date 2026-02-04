-- ===============================
-- BASE DE DATOS OPTIMIZADA CON ÍNDICES
-- Sistema de Gestión de Películas
-- LISTO PARA INYECCIÓN MASIVA
-- ===============================

-- Eliminar base de datos si existe
DROP DATABASE IF EXISTS peliculas_db;

-- Crear base de datos con encoding UTF-8
CREATE DATABASE IF NOT EXISTS peliculas_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE peliculas_db;

-- ===============================
-- CATÁLOGOS BASE
-- ===============================

CREATE TABLE Coleccion (
    coleccion_id INT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    ruta_poster VARCHAR(500),
    ruta_fondo VARCHAR(500),
    INDEX idx_coleccion_nombre (nombre(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Genero (
    genero_id INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    INDEX idx_genero_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE PalabraClave (
    palabra_clave_id INT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    INDEX idx_palabra_nombre (nombre(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Productora (
    empresa_id INT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    INDEX idx_productora_nombre (nombre(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Pais (
    codigo_iso_3166 CHAR(2) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    INDEX idx_pais_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Idioma (
    codigo_iso_639_1 CHAR(2) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    INDEX idx_idioma_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
-- PERSONA
-- ===============================
CREATE TABLE Persona (
    persona_id INT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    genero VARCHAR(20),
    ruta_perfil VARCHAR(500),
    INDEX idx_persona_nombre (nombre(100)),
    INDEX idx_persona_genero (genero)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
-- PELICULA (Tabla Principal)
-- ===============================
CREATE TABLE Pelicula (
    pelicula_id INT PRIMARY KEY,
    imdb_id VARCHAR(20),
    titulo VARCHAR(500) NOT NULL,
    titulo_original VARCHAR(500) NOT NULL,
    resumen TEXT,
    eslogan VARCHAR(500),
    duracion INT,
    fecha_estreno DATE,
    presupuesto DECIMAL(15, 2),
    ingresos DECIMAL(15, 2),
    popularidad DECIMAL(10, 3),
    promedio_votos DECIMAL(3, 1),
    cantidad_votos INT,
    estado VARCHAR(50),
    pagina_web VARCHAR(500),
    ruta_poster VARCHAR(500),
    es_video BOOLEAN DEFAULT FALSE,
    es_adulto BOOLEAN DEFAULT FALSE,
    coleccion_id INT,
    
    FOREIGN KEY (coleccion_id) REFERENCES Coleccion(coleccion_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    
    INDEX idx_pelicula_titulo (titulo(100)),
    INDEX idx_pelicula_fecha (fecha_estreno),
    INDEX idx_pelicula_popularidad (popularidad DESC),
    INDEX idx_pelicula_votos (promedio_votos DESC, cantidad_votos DESC),
    INDEX idx_pelicula_imdb (imdb_id),
    INDEX idx_pelicula_estado (estado),
    INDEX idx_pelicula_coleccion (coleccion_id),
    INDEX idx_pelicula_fecha_votos (fecha_estreno, promedio_votos DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
-- RELACIONES DE PELICULA
-- ===============================

CREATE TABLE PeliculaGenero (
    pelicula_id INT NOT NULL,
    genero_id INT NOT NULL,
    
    PRIMARY KEY (pelicula_id, genero_id),
    
    FOREIGN KEY (pelicula_id) REFERENCES Pelicula(pelicula_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (genero_id) REFERENCES Genero(genero_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_genero_pelicula (genero_id, pelicula_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE PeliculaPalabraClave (
    pelicula_id INT NOT NULL,
    palabra_clave_id INT NOT NULL,
    
    PRIMARY KEY (pelicula_id, palabra_clave_id),
    
    FOREIGN KEY (pelicula_id) REFERENCES Pelicula(pelicula_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (palabra_clave_id) REFERENCES PalabraClave(palabra_clave_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_palabra_pelicula (palabra_clave_id, pelicula_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE PeliculaProductora (
    pelicula_id INT NOT NULL,
    empresa_id INT NOT NULL,
    
    PRIMARY KEY (pelicula_id, empresa_id),
    
    FOREIGN KEY (pelicula_id) REFERENCES Pelicula(pelicula_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES Productora(empresa_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_productora_pelicula (empresa_id, pelicula_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE PeliculaPais (
    pelicula_id INT NOT NULL,
    codigo_iso_3166 CHAR(2) NOT NULL,
    
    PRIMARY KEY (pelicula_id, codigo_iso_3166),
    
    FOREIGN KEY (pelicula_id) REFERENCES Pelicula(pelicula_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (codigo_iso_3166) REFERENCES Pais(codigo_iso_3166)
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_pais_pelicula (codigo_iso_3166, pelicula_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
-- REPARTO (Cast)
-- ===============================
CREATE TABLE Reparto (
    pelicula_id INT NOT NULL,
    persona_id INT NOT NULL,
    personaje VARCHAR(500) NOT NULL,
    orden_aparicion INT,
    credito_id VARCHAR(100),
    
    PRIMARY KEY (pelicula_id, persona_id, personaje(100)),
    
    FOREIGN KEY (pelicula_id) REFERENCES Pelicula(pelicula_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (persona_id) REFERENCES Persona(persona_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_reparto_persona (persona_id, pelicula_id),
    INDEX idx_reparto_orden (pelicula_id, orden_aparicion),
    INDEX idx_reparto_personaje (personaje(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
-- EQUIPO TECNICO (Crew)
-- ===============================
CREATE TABLE EquipoTecnico (
    pelicula_id INT NOT NULL,
    persona_id INT NOT NULL,
    cargo VARCHAR(200) NOT NULL,
    departamento VARCHAR(200),
    credito_id VARCHAR(100),
    
    PRIMARY KEY (pelicula_id, persona_id, cargo),
    
    FOREIGN KEY (pelicula_id) REFERENCES Pelicula(pelicula_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (persona_id) REFERENCES Persona(persona_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_equipo_persona (persona_id, pelicula_id),
    INDEX idx_equipo_cargo (cargo),
    INDEX idx_equipo_departamento (departamento),
    INDEX idx_equipo_cargo_dept (cargo, departamento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
-- CALIFICACION
-- ===============================
CREATE TABLE Calificacion (
    calificacion_id INT AUTO_INCREMENT PRIMARY KEY,
    pelicula_id INT NOT NULL,
    usuario_id INT NOT NULL,
    puntuacion DECIMAL(2, 1) NOT NULL CHECK (puntuacion >= 0 AND puntuacion <= 10),
    marca_tiempo DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (pelicula_id) REFERENCES Pelicula(pelicula_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    INDEX idx_calif_pelicula (pelicula_id, puntuacion),
    INDEX idx_calif_usuario (usuario_id, marca_tiempo),
    INDEX idx_calif_tiempo (marca_tiempo DESC),
    UNIQUE INDEX idx_calif_unique (pelicula_id, usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
-- PROCEDIMIENTOS ÚTILES
-- ===============================

DELIMITER //

CREATE PROCEDURE obtener_estadisticas()
BEGIN
    SELECT 
        'Películas' AS Tabla,
        COUNT(*) AS Total
    FROM Pelicula
    UNION ALL
    SELECT 'Géneros', COUNT(*) FROM Genero
    UNION ALL
    SELECT 'Personas', COUNT(*) FROM Persona
    UNION ALL
    SELECT 'Productoras', COUNT(*) FROM Productora
    UNION ALL
    SELECT 'Países', COUNT(*) FROM Pais
    UNION ALL
    SELECT 'Palabras Clave', COUNT(*) FROM PalabraClave
    UNION ALL
    SELECT 'Reparto', COUNT(*) FROM Reparto
    UNION ALL
    SELECT 'Equipo Técnico', COUNT(*) FROM EquipoTecnico
    UNION ALL
    SELECT 'Colecciones', COUNT(*) FROM Coleccion;
END//

CREATE PROCEDURE preparar_carga_masiva()
BEGIN
    SET SESSION foreign_key_checks = 0;
    SET SESSION unique_checks = 0;
    SET SESSION autocommit = 0;
    SELECT 'Base de datos preparada para carga masiva. Ejecute su programa Scala ahora.' AS Mensaje;
END//

CREATE PROCEDURE finalizar_carga_masiva()
BEGIN
    COMMIT;
    SET SESSION foreign_key_checks = 1;
    SET SESSION unique_checks = 1;
    SET SESSION autocommit = 1;
    SELECT 'Carga masiva finalizada. Verificaciones reactivadas.' AS Mensaje;
END//

DELIMITER ;

-- ===============================
-- INFORMACIÓN
-- ===============================

SELECT 
    '✓ Base de datos creada exitosamente' AS Estado,
    DATABASE() AS Nombre_BD,
    VERSION() AS Version_MySQL;

SHOW TABLES;

