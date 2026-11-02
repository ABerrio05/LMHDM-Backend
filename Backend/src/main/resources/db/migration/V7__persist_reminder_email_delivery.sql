ALTER TABLE notificacion
    ADD COLUMN IF NOT EXISTS id_recordatorio INTEGER REFERENCES recordatorio(id_recordatorio),
    ADD COLUMN IF NOT EXISTS asunto VARCHAR(255),
    ADD COLUMN IF NOT EXISTS detalle_error VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS intentos INTEGER NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX IF NOT EXISTS ux_notificacion_recordatorio
    ON notificacion(id_recordatorio)
    WHERE id_recordatorio IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_notificacion_usuario_fecha
    ON notificacion(id_usuario, fecha_envio DESC);
