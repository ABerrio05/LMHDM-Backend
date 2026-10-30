package com.nexo.application.service;

import com.nexo.application.ports.in.AuditLogUseCase;
import com.nexo.application.ports.out.AuditLogRepositoryPort;
import com.nexo.domain.model.AuditLog;
import java.time.LocalDateTime;
import java.util.List;

/** Caso de uso para conservar y consultar la actividad propia de cada usuario. */
public class AuditLogService implements AuditLogUseCase {
    private static final int MAX_LIMIT = 100;
    private final AuditLogRepositoryPort auditLogs;

    public AuditLogService(AuditLogRepositoryPort auditLogs) { this.auditLogs = auditLogs; }

    @Override
    public void record(Long userId, String action, String method, String path, int statusCode, long durationMs, String requestId) {
        auditLogs.save(new AuditLog(null, userId, action, method, path, statusCode, durationMs, requestId, LocalDateTime.now()));
    }

    @Override
    public List<AuditLog> listMine(Long userId, int limit) {
        return auditLogs.findRecentByUserId(userId, Math.min(Math.max(limit, 1), MAX_LIMIT));
    }
}
