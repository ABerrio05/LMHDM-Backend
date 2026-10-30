package com.nexo.infrastructure.adapters.out.persistence;

import com.nexo.application.ports.out.AuditLogRepositoryPort;
import com.nexo.domain.model.AuditLog;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class AuditLogPersistenceAdapter implements AuditLogRepositoryPort {
    private final SpringDataAuditLogRepository repository;
    public AuditLogPersistenceAdapter(SpringDataAuditLogRepository repository) { this.repository = repository; }

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogJpaEntity entity = new AuditLogJpaEntity(auditLog.userId(), auditLog.action(), auditLog.method(),
                auditLog.path(), auditLog.statusCode(), auditLog.durationMs(), auditLog.requestId(), auditLog.createdAt());
        return toDomain(repository.save(entity));
    }

    @Override
    public List<AuditLog> findRecentByUserId(Long userId, int limit) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit)).stream().map(this::toDomain).toList();
    }

    private AuditLog toDomain(AuditLogJpaEntity entity) {
        return new AuditLog(entity.getId(), entity.getUserId(), entity.getAction(), entity.getMethod(), entity.getPath(),
                entity.getStatusCode(), entity.getDurationMs(), entity.getRequestId(), entity.getCreatedAt());
    }
}
