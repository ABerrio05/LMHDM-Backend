package com.nexo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nexo.application.ports.out.EmailNotificationRepositoryPort;
import com.nexo.domain.model.EmailNotification;
import org.junit.jupiter.api.Test;

class PlatformNotificationMetricsServiceTest {

    @Test
    void summarizesEachDeliveryStatusWithoutExposingMessageContent() {
        EmailNotificationRepositoryPort notifications = mock(EmailNotificationRepositoryPort.class);
        when(notifications.countByStatus(EmailNotification.SENT)).thenReturn(7L);
        when(notifications.countByStatus(EmailNotification.FAILED)).thenReturn(2L);
        when(notifications.countByStatus(EmailNotification.PENDING)).thenReturn(3L);

        var summary = new PlatformNotificationMetricsService(notifications).summary();

        assertEquals(7L, summary.sent());
        assertEquals(2L, summary.failed());
        assertEquals(3L, summary.pending());
        assertEquals(12L, summary.total());
    }
}
