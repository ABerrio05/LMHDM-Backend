package com.nexo.application.ports.in;

import com.nexo.domain.model.AuditLog;
import java.util.List;

public interface AuditLogUseCase {
    void record(Long userId, String action, String method, String path, int statusCode, long durationMs, String requestId);
    List<AuditLog> listMine(Long userId, int limit);
}
