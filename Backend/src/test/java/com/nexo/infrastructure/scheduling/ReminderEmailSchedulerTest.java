package com.nexo.infrastructure.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nexo.application.ports.out.EmailDeliveryPort;
import com.nexo.application.ports.out.EmailNotificationRepositoryPort;
import com.nexo.application.ports.out.ReminderRepositoryPort;
import com.nexo.application.ports.out.RecurrenceRepositoryPort;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.application.ports.out.UserRepositoryPort;
import com.nexo.domain.model.EmailNotification;
import com.nexo.domain.model.Reminder;
import com.nexo.domain.model.Recurrence;
import com.nexo.domain.model.Task;
import com.nexo.domain.model.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReminderEmailSchedulerTest {

    @Test
    void sendsDueReminderOnlyOnceAndPersistsSuccessfulDelivery() {
        InMemoryNotifications notifications = new InMemoryNotifications();
        RecordingEmailDelivery email = new RecordingEmailDelivery(true, false);
        ReminderEmailScheduler scheduler = scheduler(notifications, email);

        scheduler.deliverDueReminders();
        scheduler.deliverDueReminders();

        assertEquals(1, email.recipients.size());
        assertEquals("ana@nexo.test", email.recipients.getFirst());
        assertEquals(EmailNotification.SENT, notifications.data.getFirst().status());
        assertEquals(1, notifications.data.getFirst().attempts());
    }

    @Test
    void persistsFailedDeliveryWithoutStoppingTheScheduler() {
        InMemoryNotifications notifications = new InMemoryNotifications();
        ReminderEmailScheduler scheduler = scheduler(notifications, new RecordingEmailDelivery(true, true));

        scheduler.deliverDueReminders();

        assertEquals(1, notifications.data.size());
        assertEquals(EmailNotification.FAILED, notifications.data.getFirst().status());
        assertTrue(notifications.data.getFirst().errorDetail().contains("SMTP unavailable"));
    }

    @Test
    void retriesTransientFailuresAndRecordsTheSuccessfulSecondAttempt() {
        InMemoryNotifications notifications = new InMemoryNotifications();
        FlakyEmailDelivery email = new FlakyEmailDelivery();
        ReminderEmailScheduler scheduler = new ReminderEmailScheduler(
                new SingleReminderRepository(new Reminder(4L, LocalDateTime.now().minusMinutes(1),
                        "No olvidar la entrega", 10L)),
                new SingleTaskRepository(new Task(10L, "Entregar avance", "", null, Task.Priority.ALTA,
                        Task.TaskStatus.PENDIENTE, 7L, null, null)),
                new SingleUserRepository(new User(7L, "Ana", "ana@nexo.test", "hash", LocalDateTime.now())),
                notifications, email, new NoOpRecurrences(), 3, 0);

        scheduler.deliverDueReminders();
        scheduler.deliverDueReminders();

        assertEquals(2, email.calls);
        assertEquals(EmailNotification.SENT, notifications.data.getFirst().status());
        assertEquals(2, notifications.data.getFirst().attempts());
    }

    @Test
    void doesNotCreateDeliveryRecordsWhenEmailIsDisabled() {
        InMemoryNotifications notifications = new InMemoryNotifications();
        RecordingEmailDelivery email = new RecordingEmailDelivery(false, false);
        ReminderEmailScheduler scheduler = scheduler(notifications, email);

        scheduler.deliverDueReminders();

        assertTrue(notifications.data.isEmpty());
        assertTrue(email.recipients.isEmpty());
    }

    private ReminderEmailScheduler scheduler(InMemoryNotifications notifications, EmailDeliveryPort email) {
        Reminder due = new Reminder(4L, LocalDateTime.now().minusMinutes(1), "No olvidar la entrega", 10L);
        Task task = new Task(10L, "Entregar avance", "", null, Task.Priority.ALTA,
                Task.TaskStatus.PENDIENTE, 7L, null, null);
        User user = new User(7L, "Ana", "ana@nexo.test", "hash", LocalDateTime.now());
        return new ReminderEmailScheduler(new SingleReminderRepository(due), new SingleTaskRepository(task),
                new SingleUserRepository(user), notifications, email, new NoOpRecurrences());
    }

    private static final class RecordingEmailDelivery implements EmailDeliveryPort {
        private final boolean configured;
        private final boolean fails;
        private final List<String> recipients = new ArrayList<>();

        private RecordingEmailDelivery(boolean configured, boolean fails) {
            this.configured = configured;
            this.fails = fails;
        }

        @Override public boolean isConfigured() { return configured; }
        @Override public void send(String recipient, String subject, String content) {
            recipients.add(recipient);
            if (fails) throw new IllegalStateException("SMTP unavailable");
        }
    }

    private static final class FlakyEmailDelivery implements EmailDeliveryPort {
        private int calls;
        @Override public boolean isConfigured() { return true; }
        @Override public void send(String recipient, String subject, String content) {
            calls++;
            if (calls == 1) throw new IllegalStateException("SMTP temporalmente no disponible");
        }
    }

    private static final class InMemoryNotifications implements EmailNotificationRepositoryPort {
        private final List<EmailNotification> data = new ArrayList<>();

        @Override public Optional<EmailNotification> findByReminderId(Long reminderId) {
            return data.stream().filter(item -> item.reminderId().equals(reminderId)).findFirst();
        }
        @Override public EmailNotification save(EmailNotification notification) {
            data.removeIf(item -> item.reminderId().equals(notification.reminderId()));
            EmailNotification saved = new EmailNotification(1L, notification.reminderId(), notification.userId(),
                    notification.taskId(), notification.subject(), notification.status(), notification.attempts(),
                    notification.sentAt(), notification.errorDetail());
            data.add(saved);
            return saved;
        }
        @Override public void deleteByReminderId(Long reminderId) {
            data.removeIf(item -> item.reminderId().equals(reminderId));
        }
    }

    private record SingleReminderRepository(Reminder reminder) implements ReminderRepositoryPort {
        @Override public Reminder save(Reminder value) { return value; }
        @Override public List<Reminder> findAllByTaskId(Long taskId) { return List.of(reminder); }
        @Override public Optional<Reminder> findById(Long reminderId) { return Optional.of(reminder); }
        @Override public List<Reminder> findDueBeforeOrAt(LocalDateTime dateTime) {
            return reminder.scheduledAt().isAfter(dateTime) ? List.of() : List.of(reminder);
        }
        @Override public void delete(Reminder value) { }
    }

    private static final class NoOpRecurrences implements RecurrenceRepositoryPort {
        @Override public Recurrence save(Recurrence recurrence) { return new Recurrence(1L, recurrence.frequency(), recurrence.interval(), recurrence.endDate()); }
        @Override public Optional<Recurrence> findById(Long recurrenceId) { return Optional.empty(); }
        @Override public void deleteById(Long recurrenceId) { }
    }

    private record SingleTaskRepository(Task task) implements TaskRepositoryPort {
        @Override public Task save(Task value) { return value; }
        @Override public List<Task> findAllByOwnerId(Long ownerId) { return List.of(task); }
        @Override public List<Task> findAllBySpaceId(Long spaceId) { return List.of(); }
        @Override public Optional<Task> findById(Long taskId) { return Optional.of(task); }
        @Override public void delete(Task value) { }
    }

    private record SingleUserRepository(User user) implements UserRepositoryPort {
        @Override public boolean existsByEmail(String email) { return user.email().equals(email); }
        @Override public Optional<User> findByEmail(String email) { return Optional.of(user); }
        @Override public Optional<User> findById(Long userId) { return Optional.of(user); }
        @Override public User save(User value) { return value; }
    }
}
