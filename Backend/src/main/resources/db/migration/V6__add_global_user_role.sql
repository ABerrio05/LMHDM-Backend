ALTER TABLE usuario
    ADD COLUMN IF NOT EXISTS rol_global VARCHAR(30) NOT NULL DEFAULT 'USER';

ALTER TABLE usuario
    ADD CONSTRAINT chk_usuario_rol_global CHECK (rol_global IN ('USER', 'DEVELOPER'));
