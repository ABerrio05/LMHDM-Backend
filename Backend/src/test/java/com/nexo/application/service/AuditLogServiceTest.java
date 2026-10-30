package com.nexo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexo.application.ports.out.AuditLogRepositoryPort;
import com.nexo.domain.model.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuditLogServiceTest {
    @Test
    void storesAnAuditRecordAndLimitsTheHistoryRequest() {
        AuditLogRepositoryPort repository = mock(AuditLogRepositoryPort.class);
        AuditLogService service = new AuditLogService(repository);
        AuditLog entry = new AuditLog(1L, 5L, "POST /api/tasks", "POST", "/api/tasks", 201, 12L,
                "request-1", LocalDateTime.now());
        when(repository.findRecentByUserId(5L, 100)).thenReturn(List.of(entry));

        service.record(5L, "POST /api/tasks", "POST", "/api/tasks", 201, 12L, "request-1");
        assertEquals(List.of(entry), service.listMine(5L, 500));

        verify(repository).save(any(AuditLog.class));
        verify(repository).findRecentByUserId(5L, 100);
    }
}
