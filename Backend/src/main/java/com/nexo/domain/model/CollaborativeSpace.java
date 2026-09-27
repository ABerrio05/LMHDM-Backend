package com.nexo.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Espacio compartido que agrupa tareas de varios usuarios. */
public record CollaborativeSpace(Long id, String name, String description, Long creatorId, LocalDateTime createdAt) {
    public CollaborativeSpace {
        Objects.requireNonNull(name, "El nombre del espacio es obligatorio");
        Objects.requireNonNull(creatorId, "El creador es obligatorio");
        if (name.isBlank()) throw new IllegalArgumentException("El nombre del espacio no puede estar vacío");
    }
}
