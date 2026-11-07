package com.nexo.application.service;

import com.nexo.application.ports.in.NotificationPreferenceUseCase;
import com.nexo.application.ports.out.EmailDeliveryPort;
import com.nexo.domain.model.CollaborativeSpace;
import com.nexo.domain.model.NotificationPreference;
import com.nexo.domain.model.SpaceMember;
import com.nexo.domain.model.Task;
import com.nexo.domain.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivityEmailNotificationServiceTest {

    private static final LocalDateTime DATE = LocalDateTime.of(2026, 11, 5, 10, 0);
    private static final User OWNER = new User(1L, "Andrea", "andrea@example.com", "password-hash", DATE);
    private static final User MEMBER = new User(2L, "Felipe", "felipe@example.com", "password-hash", DATE);

    @Test
    void sendsEveryKindOfNotificationWhenActivityEmailsAreEnabled() {
        RecordingDelivery delivery = new RecordingDelivery(true, false);
        ActivityEmailNotificationService service = new ActivityEmailNotificationService(delivery, new Preferences(true));
        Task task = task(DATE.plusDays(1));
        CollaborativeSpace space = space("Planificar la entrega final");

        service.accountCreated(OWNER);
        service.taskCreated(OWNER, task);
        service.taskUpdated(OWNER, task);
        service.taskDeleted(OWNER, task);
        service.sharedTaskCreated(MEMBER, OWNER, task);
        service.sharedTaskCompleted(MEMBER, OWNER, task);
        service.sharedTaskUpdated(MEMBER, OWNER, task);
        service.sharedTaskDeleted(MEMBER, OWNER, task);
        service.spaceCreated(OWNER, space);
        service.spaceInvitation(MEMBER, OWNER, space, SpaceMember.Role.MIEMBRO);
        service.pendingSpaceInvitation(OWNER, space, "nuevo@example.com", SpaceMember.Role.INVITADO, "https://nexo.test/register");

        assertEquals(11, delivery.messages.size());
        assertTrue(delivery.subjects().stream().anyMatch(subject -> subject.contains("cuenta creada")));
        assertTrue(delivery.subjects().stream().anyMatch(subject -> subject.contains("tarea creada")));
        assertTrue(delivery.subjects().stream().anyMatch(subject -> subject.contains("invitación")));
    }

    @Test
    void skipsOnlyOptionalActivityEmailsWhenTheUserDisabledThem() {
        RecordingDelivery delivery = new RecordingDelivery(true, false);
        ActivityEmailNotificationService service = new ActivityEmailNotificationService(delivery, new Preferences(false));

        service.taskCreated(OWNER, task(DATE));
        service.accountCreated(OWNER);
        service.spaceInvitation(MEMBER, OWNER, space(null), SpaceMember.Role.MIEMBRO);
        service.pendingSpaceInvitation(OWNER, space(null), "nuevo@example.com", SpaceMember.Role.INVITADO, "https://nexo.test/register");

        assertEquals(3, delivery.messages.size());
    }

    @Test
    void usesFriendlyFallbacksForMissingDatesAndDescriptions() {
        RecordingDelivery delivery = new RecordingDelivery(true, false);
        ActivityEmailNotificationService service = new ActivityEmailNotificationService(delivery);

        service.taskCreated(OWNER, task(null));
        service.spaceCreated(OWNER, space(" "));
        service.spaceInvitation(MEMBER, OWNER, space(null), SpaceMember.Role.MIEMBRO);

        assertTrue(delivery.contents().stream().anyMatch(content -> content.contains("Sin fecha límite")));
        assertTrue(delivery.contents().stream().anyMatch(content -> content.contains("No proporcionada")));
    }

    @Test
    void doesNotQueueEmailsWhenDeliveryIsNotConfigured() {
        RecordingDelivery delivery = new RecordingDelivery(false, false);
        ActivityEmailNotificationService service = new ActivityEmailNotificationService(delivery);

        service.accountCreated(OWNER);
        service.taskCreated(OWNER, task(DATE));

        assertTrue(delivery.messages.isEmpty());
    }

    @Test
    void containsDeliveryFailuresAndUsesTheProvidedExecutor() {
        RecordingDelivery delivery = new RecordingDelivery(true, true);
        RecordingExecutor executor = new RecordingExecutor();
        ActivityEmailNotificationService service = new ActivityEmailNotificationService(delivery, null, executor);

        assertDoesNotThrow(() -> service.accountCreated(OWNER));
        assertEquals(1, executor.executions);
    }

    private static Task task(LocalDateTime dueDate) {
        return new Task(10L, "Preparar presentación", "Organizar la demo", dueDate,
                Task.Priority.ALTA, Task.TaskStatus.PENDIENTE, OWNER.id(), 20L, null);
    }

    private static CollaborativeSpace space(String description) {
        return new CollaborativeSpace(20L, "Equipo NEXO", description, OWNER.id(), DATE);
    }

    private record EmailMessage(String recipient, String subject, String content) {
    }

    private static final class RecordingDelivery implements EmailDeliveryPort {
        private final boolean configured;
        private final boolean fails;
        private final List<EmailMessage> messages = new ArrayList<>();

        private RecordingDelivery(boolean configured, boolean fails) {
            this.configured = configured;
            this.fails = fails;
        }

        @Override
        public boolean isConfigured() {
            return configured;
        }

        @Override
        public void send(String recipient, String subject, String content) {
            if (fails) {
                throw new IllegalStateException("SMTP temporalmente no disponible");
            }
            messages.add(new EmailMessage(recipient, subject, content));
        }

        private List<String> subjects() {
            return messages.stream().map(EmailMessage::subject).toList();
        }

        private List<String> contents() {
            return messages.stream().map(EmailMessage::content).toList();
        }
    }

    private static final class Preferences implements NotificationPreferenceUseCase {
        private final boolean enabled;

        private Preferences(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public NotificationPreference getMine(Long userId) {
            return new NotificationPreference(userId, enabled, DATE);
        }

        @Override
        public NotificationPreference updateMine(Long userId, boolean activityEmailsEnabled) {
            return new NotificationPreference(userId, activityEmailsEnabled, DATE);
        }

        @Override
        public boolean activityEmailsEnabledFor(Long userId) {
            return enabled;
        }
    }

    private static final class RecordingExecutor implements Executor {
        private int executions;

        @Override
        public void execute(Runnable command) {
            executions++;
            command.run();
        }
    }
}
