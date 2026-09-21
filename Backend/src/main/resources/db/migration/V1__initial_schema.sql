CREATE TABLE usuario (
    id_usuario SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE espacio_colaborativo (
    id_espacio SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    id_usuario_creador INTEGER NOT NULL REFERENCES usuario(id_usuario),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE miembro_espacio (
    id_miembro SERIAL PRIMARY KEY,
    id_espacio INTEGER NOT NULL REFERENCES espacio_colaborativo(id_espacio),
    id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario),
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('administrador', 'moderador', 'miembro', 'invitado')),
    fecha_ingreso TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id_espacio, id_usuario)
);

CREATE TABLE recurrencia (
    id_recurrencia SERIAL PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('diaria', 'semanal', 'personalizada')),
    intervalo INTEGER NOT NULL CHECK (intervalo > 0),
    fecha_fin DATE
);

CREATE TABLE tarea (
    id_tarea SERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    fecha_vencimiento TIMESTAMP,
    prioridad VARCHAR(10) NOT NULL CHECK (prioridad IN ('alta', 'media', 'baja')),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('pendiente', 'en_progreso', 'completada')),
    id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario),
    id_espacio INTEGER REFERENCES espacio_colaborativo(id_espacio),
    id_recurrencia INTEGER UNIQUE REFERENCES recurrencia(id_recurrencia)
);

CREATE TABLE nota (
    id_nota SERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    contenido TEXT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario)
);

CREATE TABLE recordatorio (
    id_recordatorio SERIAL PRIMARY KEY,
    fecha_hora TIMESTAMP NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    id_tarea INTEGER NOT NULL REFERENCES tarea(id_tarea)
);

CREATE TABLE notificacion (
    id_notificacion SERIAL PRIMARY KEY,
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('push', 'correo')),
    fecha_envio TIMESTAMP,
    estado_envio VARCHAR(10) NOT NULL CHECK (estado_envio IN ('enviada', 'fallida', 'pendiente')),
    id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario),
    id_tarea INTEGER REFERENCES tarea(id_tarea)
);

CREATE INDEX idx_tarea_usuario ON tarea(id_usuario);
CREATE INDEX idx_tarea_espacio ON tarea(id_espacio);
CREATE INDEX idx_recordatorio_fecha_hora ON recordatorio(fecha_hora);
