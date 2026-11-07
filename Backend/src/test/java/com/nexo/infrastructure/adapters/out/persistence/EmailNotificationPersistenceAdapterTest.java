package com.nexo.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexo.domain.model.EmailNotification;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class EmailNotificationPersistenceAdapterTest {

    @Mock
    private SpringDataEmailNotificationRepository repository;

    private EmailNotificationPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new EmailNotificationPersistenceAdapter(repository);
    }

    @Test
    void createsAndMapsANotification() {
        EmailNotification notification = new EmailNotification(null, 8L, 4L, 12L,
                "Recordatorio", "pendiente", 0, null, null);
        when(repository.findByReminderId(8L)).thenReturn(Optional.empty());
        when(repository.save(any(EmailNotificationJpaEntity.class))).thenAnswer(invocation -> {
            EmailNotificationJpaEntity saved = invocation.getArgument(0);
            setId(saved, 30L);
            return saved;
        });

        EmailNotification result = adapter.save(notification);

        ArgumentCaptor<EmailNotificationJpaEntity> captor =
                ArgumentCaptor.forClass(EmailNotificationJpaEntity.class);
        verify(repository).save(captor.capture());
        assertEquals(8L, captor.getValue().getReminderId());
        assertEquals(30L, result.id());
        assertEquals("Recordatorio", result.subject());
        assertEquals("pendiente", result.status());
    }

    @Test
    void updatesAnExistingNotification() {
        EmailNotificationJpaEntity existing = new EmailNotificationJpaEntity(8L, 4L, 12L,
                "Anterior", "pendiente", 0, null, null);
        setId(existing, 31L);
        LocalDateTime sentAt = LocalDateTime.of(2026, 11, 7, 12, 0);
        EmailNotification sent = new EmailNotification(31L, 8L, 4L, 12L,
                "Enviado", "enviado", 1, sentAt, null);
        when(repository.findByReminderId(8L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        EmailNotification result = adapter.save(sent);

        assertEquals("Enviado", existing.getSubject());
        assertEquals("enviado", existing.getStatus());
        assertEquals(1, existing.getAttempts());
        assertEquals(sentAt, existing.getSentAt());
        assertEquals("enviado", result.status());
    }

    @Test
    void findsDeletesAndCountsNotifications() {
        EmailNotificationJpaEntity entity = new EmailNotificationJpaEntity(50L, 5L, 13L,
                "Aviso", "fallido", 2, null, "smtp timeout");
        setId(entity, 40L);
        when(repository.findByReminderId(50L)).thenReturn(Optional.of(entity));
        when(repository.countByStatus("fallido")).thenReturn(3L);

        Optional<EmailNotification> result = adapter.findByReminderId(50L);

        assertTrue(result.isPresent());
        assertEquals("smtp timeout", result.orElseThrow().errorDetail());
        assertEquals(3L, adapter.countByStatus("fallido"));
        adapter.deleteByReminderId(50L);
        verify(repository).deleteByReminderId(50L);
    }

    private static void setId(Object target, Long id) {
        try {
            Field field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
