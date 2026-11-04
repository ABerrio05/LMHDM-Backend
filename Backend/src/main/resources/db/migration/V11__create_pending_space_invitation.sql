CREATE TABLE invitacion_espacio (
    id_invitacion SERIAL PRIMARY KEY,
    id_espacio INTEGER NOT NULL REFERENCES espacio_colaborativo(id_espacio) ON DELETE CASCADE,
    correo VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('administrador', 'moderador', 'miembro', 'invitado')),
    id_usuario_invitador INTEGER NOT NULL REFERENCES usuario(id_usuario),
    token VARCHAR(128) NOT NULL UNIQUE,
    fecha_expiracion TIMESTAMP NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_aceptacion TIMESTAMP
);

CREATE INDEX idx_invitacion_espacio_correo ON invitacion_espacio(correo);
