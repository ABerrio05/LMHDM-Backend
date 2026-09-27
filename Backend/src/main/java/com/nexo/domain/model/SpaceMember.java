package com.nexo.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Pertenencia de un usuario a un espacio colaborativo. */
public record SpaceMember(Long id, Long spaceId, Long userId, Role role, LocalDateTime joinedAt) {
    public SpaceMember {
        Objects.requireNonNull(spaceId, "El espacio es obligatorio");
        Objects.requireNonNull(userId, "El usuario es obligatorio");
        Objects.requireNonNull(role, "El rol es obligatorio");
    }

    public enum Role { ADMINISTRADOR, MODERADOR, MIEMBRO, INVITADO }
}
