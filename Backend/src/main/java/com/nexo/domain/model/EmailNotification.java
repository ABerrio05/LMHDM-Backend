package com.nexo.domain.model;

import java.time.LocalDateTime;

/** Registro persistente del intento de entrega de un recordatorio por correo. */
public record EmailNotification(
        Long id,
        Long reminderId,
        Long userId,
        Long taskId,
        String subject,
        String status,
        int attempts,
        LocalDateTime sentAt,
        String errorDetail
) {
    public static final String PENDING = "pendiente";
    public static final String SENT = "enviada";
    public static final String FAILED = "fallida";
}
