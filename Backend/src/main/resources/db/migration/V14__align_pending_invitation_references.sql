-- Las referencias de la entidad usan Long. PostgreSQL conserva las relaciones y los datos al ampliar INTEGER a BIGINT.
ALTER TABLE invitacion_espacio
    ALTER COLUMN id_espacio TYPE BIGINT,
    ALTER COLUMN id_usuario_invitador TYPE BIGINT;
