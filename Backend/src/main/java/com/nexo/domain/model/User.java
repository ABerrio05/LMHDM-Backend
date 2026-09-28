package com.nexo.domain.model;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

/** Usuario del dominio. Su contraseña siempre llega cifrada desde la aplicación. */
public record User(Long id, String name, String email, String passwordHash, LocalDateTime registeredAt) {
    public User {
        Objects.requireNonNull(name, "El nombre es obligatorio");
        Objects.requireNonNull(email, "El correo es obligatorio");
        Objects.requireNonNull(passwordHash, "La contraseña cifrada es obligatoria");
        name = name.trim();
        email = email.trim().toLowerCase(Locale.ROOT);
        if (name.isBlank() || email.isBlank()) {
            throw new IllegalArgumentException("Nombre y correo son obligatorios");
        }
    }
}
