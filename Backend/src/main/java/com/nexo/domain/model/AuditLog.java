package com.nexo.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Registro de auditoría seguro de una acción HTTP realizada por un usuario. */
public record AuditLog(Long id, Long userId, String action, String method, String path,
                       int statusCode, long durationMs, String requestId, LocalDateTime createdAt) {
    public AuditLog {
        Objects.requireNonNull(userId, "El usuario del registro es obligatorio");
        Objects.requireNonNull(action, "La acción es obligatoria");
        Objects.requireNonNull(method, "El método es obligatorio");
        Objects.requireNonNull(path, "La ruta es obligatoria");
        Objects.requireNonNull(requestId, "El identificador de petición es obligatorio");
        Objects.requireNonNull(createdAt, "La fecha del registro es obligatoria");
        if (action.isBlank() || method.isBlank() || path.isBlank() || requestId.isBlank()) {
            throw new IllegalArgumentException("Los datos del registro de auditoría son obligatorios");
        }
        if (statusCode < 100 || statusCode > 599 || durationMs < 0) {
            throw new IllegalArgumentException("El estado o la duración del registro son inválidos");
        }
    }
}
