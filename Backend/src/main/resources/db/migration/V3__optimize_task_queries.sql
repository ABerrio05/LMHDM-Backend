-- Índices alineados con las consultas reales de NEXO.
-- Las pantallas de tareas y espacios filtran por propietario/espacio y ordenan por vencimiento.
CREATE INDEX IF NOT EXISTS idx_tarea_usuario_vencimiento
    ON tarea (id_usuario, fecha_vencimiento ASC NULLS LAST);

CREATE INDEX IF NOT EXISTS idx_tarea_espacio_vencimiento
    ON tarea (id_espacio, fecha_vencimiento ASC NULLS LAST);

-- Consultas frecuentes de las nuevas funcionalidades.
CREATE INDEX IF NOT EXISTS idx_nota_usuario_creacion
    ON nota (id_usuario, fecha_creacion DESC);

CREATE INDEX IF NOT EXISTS idx_recordatorio_tarea_fecha
    ON recordatorio (id_tarea, fecha_hora ASC);

CREATE INDEX IF NOT EXISTS idx_miembro_usuario
    ON miembro_espacio (id_usuario);
