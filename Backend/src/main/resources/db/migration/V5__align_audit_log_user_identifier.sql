ALTER TABLE auditoria_log DROP CONSTRAINT IF EXISTS auditoria_log_id_usuario_fkey;

ALTER TABLE auditoria_log
    ALTER COLUMN id_usuario TYPE BIGINT;

ALTER TABLE auditoria_log
    ADD CONSTRAINT auditoria_log_id_usuario_fkey
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario);
