package com.nexo.domain.model;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

/** Invitación para una dirección que aún no tiene cuenta en NEXO. */
public record PendingSpaceInvitation(Long id, Long spaceId, String email, SpaceMember.Role role,
                                     Long inviterId, String token, LocalDateTime expiresAt,
                                     LocalDateTime createdAt, LocalDateTime acceptedAt) {
    public PendingSpaceInvitation {
        Objects.requireNonNull(spaceId, "El espacio es obligatorio");
        Objects.requireNonNull(email, "El correo es obligatorio");
        Objects.requireNonNull(role, "El rol es obligatorio");
        Objects.requireNonNull(inviterId, "El invitador es obligatorio");
        Objects.requireNonNull(token, "El token es obligatorio");
        Objects.requireNonNull(expiresAt, "La expiración es obligatoria");
        email = email.trim().toLowerCase(Locale.ROOT);
    }
    public boolean isUsableAt(LocalDateTime now) { return acceptedAt == null && expiresAt.isAfter(now); }
}
