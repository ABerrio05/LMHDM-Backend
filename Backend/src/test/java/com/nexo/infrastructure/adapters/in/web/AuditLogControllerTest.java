package com.nexo.infrastructure.adapters.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexo.application.ports.in.AuditLogUseCase;
import com.nexo.domain.model.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuditLogControllerTest {
    @Test
    void returnsOnlyTheHistoryRequestedForTheAuthenticatedUser() {
        AuditLogUseCase useCase = mock(AuditLogUseCase.class);
        AuditLog entry = new AuditLog(1L, 8L, "PATCH /api/tasks/3/status", "PATCH", "/api/tasks/3/status",
                200, 15L, "request-3", LocalDateTime.now());
        when(useCase.listMine(8L, 25)).thenReturn(List.of(entry));
        AuditLogController controller = new AuditLogController(useCase);

        assertEquals(List.of(entry), controller.listMine(8L, 25));
        verify(useCase).listMine(8L, 25);
    }
}
