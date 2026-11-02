package com.nexo.application.service;

import com.nexo.application.ports.out.EmailNotificationRepositoryPort;
import com.nexo.domain.model.EmailNotification;
import org.springframework.stereotype.Service;

/** Métricas de entrega de correo visibles solamente en el panel técnico. */
@Service
public class PlatformNotificationMetricsService {
    private final EmailNotificationRepositoryPort notifications;

    public PlatformNotificationMetricsService(EmailNotificationRepositoryPort notifications) {
        this.notifications = notifications;
    }

    public NotificationMetrics summary() {
        long sent = notifications.countByStatus(EmailNotification.SENT);
        long failed = notifications.countByStatus(EmailNotification.FAILED);
        long pending = notifications.countByStatus(EmailNotification.PENDING);
        return new NotificationMetrics(sent, failed, pending, sent + failed + pending);
    }

    public record NotificationMetrics(long sent, long failed, long pending, long total) { }
}
