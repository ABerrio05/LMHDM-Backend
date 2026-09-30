package com.nexo.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public record Reminder(Long id, LocalDateTime scheduledAt, String message, Long taskId) {
    public Reminder {
        Objects.requireNonNull(scheduledAt, "La fecha del recordatorio es obligatoria");
        Objects.requireNonNull(message, "El mensaje es obligatorio");
        Objects.requireNonNull(taskId, "La tarea es obligatoria");
        if (message.isBlank()) throw new IllegalArgumentException("El mensaje no puede estar vacío");
    }
}
