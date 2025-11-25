-- DATOS DE PRUEBA MEJORADOS
INSERT INTO usuarios (username, password, nombres, apellidos, email, rol, activo) VALUES 
('admin', '{noop}admin123', 'Administrador', 'Sistema', 'admin@uts.edu.co', 'ADMIN', true);

INSERT INTO usuarios (username, password, nombres, apellidos, email, rol, activo) VALUES 
('coordinacion', '{noop}coord123', 'Coordinador', 'Académico', 'coordinacion@uts.edu.co', 'COORDINACION', true);

INSERT INTO usuarios (username, password, nombres, apellidos, email, rol, activo) VALUES 
('estudiante', '{noop}estudiante123', 'Estudiante', 'Ejemplo', 'estudiante@uts.edu.co', 'ESTUDIANTE', true);

-- NUEVOS USUARIOS PARA PROBAR
INSERT INTO usuarios (username, password, nombres, apellidos, email, rol, activo) VALUES 
('maria.garcia', '{noop}maria456', 'María Fernanda', 'García López', 'maria.garcia@uts.edu.co', 'ESTUDIANTE', true);

INSERT INTO usuarios (username, password, nombres, apellidos, email, rol, activo) VALUES 
('carlos.lopez', '{noop}carlos789', 'Carlos Andrés', 'López Martínez', 'carlos.lopez@uts.edu.co', 'ESTUDIANTE', true);

INSERT INTO usuarios (username, password, nombres, apellidos, email, rol, activo) VALUES 
('ana.rodriguez', '{noop}ana321', 'Ana Patricia', 'Rodríguez Silva', 'ana.rodriguez@uts.edu.co', 'COORDINACION', true);

-- Insertar algunos estudiantes para relacionar
INSERT INTO estudiantes (numero_documento, nombres, apellidos, email, programa_academico, semestre, tipo_prueba) VALUES 
('1005', 'María Fernanda', 'García López', 'maria.garcia@uts.edu.co', 'Ingeniería de Software', 10, 'SABER_PRO');

INSERT INTO estudiantes (numero_documento, nombres, apellidos, email, programa_academico, semestre, tipo_prueba) VALUES 
('1006', 'Carlos Andrés', 'López Martínez', 'carlos.lopez@uts.edu.co', 'Tecnología en Sistemas', 6, 'SABER_TT');

-- ✅ RELACIONAR USUARIOS CON ESTUDIANTES (ESTO ES CLAVE)
UPDATE usuarios SET estudiante_id = '1005' WHERE username = 'maria.garcia';
UPDATE usuarios SET estudiante_id = '1006' WHERE username = 'carlos.lopez';
UPDATE usuarios SET estudiante_id = '1005' WHERE username = 'estudiante'; -- Opcional: relacionar el usuario genérico

-- ✅ AGREGAR ALGUNOS RESULTADOS DE PRUEBA PARA VER DATOS
INSERT INTO resultados_prueba (estudiante_id, tipo_prueba, puntaje_global, anio_prueba, periodo, estado, observaciones, fecha_registro) VALUES 
('1005', 'SABER_PRO', 185, 2024, 1, 'APROBADO', 'Prueba aprobada con puntaje 185', '2024-01-20 10:00:00'),
('1006', 'SABER_TT', 75, 2024, 1, 'REPROBADO', 'Puntaje 75 inferior a 80. Debe repetir la prueba.', '2024-01-20 10:00:00');

-- ✅ AGREGAR BENEFICIOS AUTOMÁTICOS (para probar)
INSERT INTO beneficios (estudiante_id, tipo_beneficio, nota_asignada, porcentaje_descuento, fecha_asignacion, activo) VALUES 
('1005', 'EXONERACION_TRABAJO_GRADO', 4.5, 0.0, '2024-01-21', true);