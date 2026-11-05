-- La entidad de invitaciones usa identificadores Long; se conserva la información existente.
ALTER TABLE invitacion_espacio
    ALTER COLUMN id_invitacion TYPE BIGINT;
