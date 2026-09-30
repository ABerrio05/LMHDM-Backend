package com.nexo.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Entidad pura del dominio; no depende de Spring ni de JPA. */
public record Task(
        Long id,
        String title,
        String description,
        LocalDateTime dueDate,
        Priority priority,
        TaskStatus status,
        Long ownerId,
        Long spaceId,
        Long recurrenceId
) {
    public Task {
        Objects.requireNonNull(title, "El título es obligatorio");
        Objects.requireNonNull(priority, "La prioridad es obligatoria");
        Objects.requireNonNull(status, "El estado es obligatorio");
        Objects.requireNonNull(ownerId, "El propietario es obligatorio");
        if (title.isBlank()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
    }

    public enum Priority { ALTA, MEDIA, BAJA }
    public enum TaskStatus { PENDIENTE, EN_PROGRESO, COMPLETADA }
}
