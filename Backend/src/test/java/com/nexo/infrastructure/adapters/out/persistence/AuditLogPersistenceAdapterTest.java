package com.nexo.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexo.domain.model.AuditLog;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuditLogPersistenceAdapterTest {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 30, 10, 0);

    @Test
    void savesAnAuditEntryAndMapsThePersistedEntityBackToTheDomain() throws Exception {
        SpringDataAuditLogRepository repository = mock(SpringDataAuditLogRepository.class);
        AuditLogJpaEntity persisted = entityWithId(21L);
        when(repository.save(any(AuditLogJpaEntity.class))).thenReturn(persisted);
        AuditLogPersistenceAdapter adapter = new AuditLogPersistenceAdapter(repository);

        AuditLog saved = adapter.save(logEntry());

        assertEquals(21L, saved.id());
        assertEquals(7L, saved.userId());
        assertEquals("CREATE_TASK", saved.action());
        assertEquals("POST", saved.method());
        assertEquals("/api/tasks", saved.path());
        assertEquals(201, saved.statusCode());
        assertEquals(34L, saved.durationMs());
        assertEquals("request-123", saved.requestId());
        assertEquals(CREATED_AT, saved.createdAt());
        verify(repository).save(any(AuditLogJpaEntity.class));
    }

    @Test
    void returnsTheRecentEntriesForTheRequestedUser() throws Exception {
        SpringDataAuditLogRepository repository = mock(SpringDataAuditLogRepository.class);
        AuditLogJpaEntity persisted = entityWithId(22L);
        when(repository.findByUserIdOrderByCreatedAtDesc(any(), any())).thenReturn(List.of(persisted));
        AuditLogPersistenceAdapter adapter = new AuditLogPersistenceAdapter(repository);

        List<AuditLog> entries = adapter.findRecentByUserId(7L, 5);

        assertEquals(1, entries.size());
        assertEquals(22L, entries.get(0).id());
        verify(repository).findByUserIdOrderByCreatedAtDesc(any(), any());
    }

    private AuditLogJpaEntity entityWithId(Long id) throws Exception {
        AuditLogJpaEntity entity = new AuditLogJpaEntity(7L, "CREATE_TASK", "POST", "/api/tasks", 201,
                34L, "request-123", CREATED_AT);
        Field idField = AuditLogJpaEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
        return entity;
    }

    private AuditLog logEntry() {
        return new AuditLog(null, 7L, "CREATE_TASK", "POST", "/api/tasks", 201,
                34L, "request-123", CREATED_AT);
    }
}
