package com.nexo.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/** Regla de repetición asociada a una única tarea. */
public record Recurrence(Long id, Frequency frequency, int interval, LocalDate endDate) {
    public Recurrence {
        Objects.requireNonNull(frequency, "La frecuencia es obligatoria");
        if (interval < 1 || interval > 365) {
            throw new IllegalArgumentException("El intervalo debe estar entre 1 y 365");
        }
    }

    public enum Frequency { DIARIA, SEMANAL, PERSONALIZADA }
}
