ALTER TABLE recordatorio
    ADD COLUMN IF NOT EXISTS id_recurrencia BIGINT UNIQUE
        REFERENCES recurrencia(id_recurrencia);
