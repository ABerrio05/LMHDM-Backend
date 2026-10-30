package com.nexo.application.ports.out;

import com.nexo.domain.model.AuditLog;
import java.util.List;

public interface AuditLogRepositoryPort {
    AuditLog save(AuditLog auditLog);
    List<AuditLog> findRecentByUserId(Long userId, int limit);
}
