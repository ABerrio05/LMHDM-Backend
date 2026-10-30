package com.nexo.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Nota personal del usuario, independiente de una tarea concreta. */
public record PersonalNote(Long id, String title, String content, Long ownerId, LocalDateTime createdAt) {
    public PersonalNote {
        Objects.requireNonNull(title, "El título de la nota es obligatorio");
        Objects.requireNonNull(content, "El contenido de la nota es obligatorio");
        Objects.requireNonNull(ownerId, "El propietario de la nota es obligatorio");
        if (title.isBlank() || content.isBlank()) {
            throw new IllegalArgumentException("El título y el contenido de la nota son obligatorios");
        }
    }
}
