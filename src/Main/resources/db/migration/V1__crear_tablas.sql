CREATE TABLE equipos (
    id_equipo VARCHAR(30) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    ubicacion VARCHAR(160) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    fallas_reincidentes INT NOT NULL DEFAULT 0,
    destacado BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id_equipo),
    CONSTRAINT chk_equipos_fallas CHECK (fallas_reincidentes >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tecnicos (
    id_usuario VARCHAR(30) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    departamento VARCHAR(120) NOT NULL,
    especialidad VARCHAR(120) NOT NULL,
    casos_activos INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id_usuario),
    CONSTRAINT chk_tecnicos_casos CHECK (casos_activos BETWEEN 0 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE solicitantes (
    id_usuario VARCHAR(30) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    departamento VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    PRIMARY KEY (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE solicitudes (
    id_solicitud INT NOT NULL AUTO_INCREMENT,
    descripcion_falla TEXT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    equipo_id VARCHAR(30) NOT NULL,
    solicitante_id VARCHAR(30) NOT NULL,
    tecnico_id VARCHAR(30) NULL,
    diagnostico TEXT NULL,
    solucion TEXT NULL,
    fecha_hora_creacion DATETIME NOT NULL,
    fecha_hora_cierre DATETIME NULL,
    PRIMARY KEY (id_solicitud),
    INDEX idx_solicitudes_estado_tecnico (estado, tecnico_id),
    INDEX idx_solicitudes_equipo (equipo_id),
    INDEX idx_solicitudes_solicitante (solicitante_id),
    CONSTRAINT fk_solicitudes_equipo
        FOREIGN KEY (equipo_id) REFERENCES equipos (id_equipo),
    CONSTRAINT fk_solicitudes_solicitante
        FOREIGN KEY (solicitante_id) REFERENCES solicitantes (id_usuario),
    CONSTRAINT fk_solicitudes_tecnico
        FOREIGN KEY (tecnico_id) REFERENCES tecnicos (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1001;

CREATE TABLE revisiones_preventivas (
    id_revision VARCHAR(30) NOT NULL,
    equipo_id VARCHAR(30) NOT NULL,
    fecha_programada DATE NOT NULL,
    dias_aviso_previo INT NOT NULL,
    email_supervisor VARCHAR(254) NOT NULL,
    alerta_generada BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id_revision),
    INDEX idx_revisiones_fecha_alerta (fecha_programada, alerta_generada),
    CONSTRAINT chk_revisiones_dias CHECK (dias_aviso_previo >= 0),
    CONSTRAINT fk_revisiones_equipo
        FOREIGN KEY (equipo_id) REFERENCES equipos (id_equipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
