INSERT INTO equipos (id_equipo, nombre, ubicacion, estado, fallas_reincidentes, destacado)
VALUES
    ('EQ-100', 'Impresora Industrial', 'Piso 2', 'ACTIVO', 0, FALSE),
    ('EQ-200', 'Servidor Principal', 'Data Center', 'ACTIVO', 0, FALSE);

INSERT INTO tecnicos (id_usuario, nombre, departamento, especialidad, casos_activos)
VALUES
    ('T01', 'Juan Perez', 'Mantenimiento', 'Mecánico', 0),
    ('T02', 'Maria Gomez', 'Mantenimiento', 'Electricista', 0);
