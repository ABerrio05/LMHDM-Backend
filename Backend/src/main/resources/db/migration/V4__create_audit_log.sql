CREATE TABLE auditoria_log (
    id_log BIGSERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario),
    accion VARCHAR(150) NOT NULL,
    metodo VARCHAR(10) NOT NULL,
    ruta VARCHAR(500) NOT NULL,
    codigo_estado INTEGER NOT NULL CHECK (codigo_estado BETWEEN 100 AND 599),
    duracion_ms BIGINT NOT NULL CHECK (duracion_ms >= 0),
    request_id VARCHAR(64) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_auditoria_log_usuario_fecha ON auditoria_log (id_usuario, fecha_creacion DESC);
