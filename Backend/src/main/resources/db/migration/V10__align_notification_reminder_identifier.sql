-- V7 created this foreign-key column as INTEGER while recordatorio now uses BIGINT.
-- Keep existing notification records and align the physical schema with the JPA Long mapping.
ALTER TABLE notificacion
    ALTER COLUMN id_recordatorio TYPE BIGINT
    USING id_recordatorio::BIGINT;
