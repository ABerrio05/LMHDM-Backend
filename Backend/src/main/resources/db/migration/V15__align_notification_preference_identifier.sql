-- La preferencia está asociada a un usuario cuyo identificador se representa como Long en el dominio.
ALTER TABLE preferencia_notificacion
    ALTER COLUMN id_usuario TYPE BIGINT;
