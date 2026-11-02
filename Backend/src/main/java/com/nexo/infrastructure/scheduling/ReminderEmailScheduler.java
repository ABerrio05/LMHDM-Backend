package com.nexo.infrastructure.scheduling;

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
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Revisa los recordatorios vencidos y entrega un único correo por cada uno.
 * La entrega se registra en la tabla notificacion para evitar correos duplicados.
 */
@Component
public class ReminderEmailScheduler {
    private static final Logger log = LoggerFactory.getLogger(ReminderEmailScheduler.class);
    private static final int MAX_ERROR_DETAIL_LENGTH = 900;

    private final ReminderRepositoryPort reminders;
    private final TaskRepositoryPort tasks;
    private final UserRepositoryPort users;
    private final EmailNotificationRepositoryPort notifications;
    private final EmailDeliveryPort emailDelivery;
    private final RecurrenceRepositoryPort recurrences;
    private final int maxAttempts;
    private final long retryDelayMs;
    private final AtomicBoolean configurationWarningShown = new AtomicBoolean(false);

    @Autowired
    public ReminderEmailScheduler(ReminderRepositoryPort reminders, TaskRepositoryPort tasks, UserRepositoryPort users,
                                  EmailNotificationRepositoryPort notifications, EmailDeliveryPort emailDelivery,
                                  RecurrenceRepositoryPort recurrences,
                                  @Value("${app.notifications.email.max-attempts:3}") int maxAttempts,
                                  @Value("${app.notifications.email.retry-delay-ms:300000}") long retryDelayMs) {
        this.reminders = reminders;
        this.tasks = tasks;
        this.users = users;
        this.notifications = notifications;
        this.emailDelivery = emailDelivery;
        this.recurrences = recurrences;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.retryDelayMs = Math.max(0, retryDelayMs);
    }

    ReminderEmailScheduler(ReminderRepositoryPort reminders, TaskRepositoryPort tasks, UserRepositoryPort users,
                           EmailNotificationRepositoryPort notifications, EmailDeliveryPort emailDelivery,
                           RecurrenceRepositoryPort recurrences) {
        this(reminders, tasks, users, notifications, emailDelivery, recurrences, 3, 300000);
    }

    @Scheduled(fixedDelayString = "${app.notifications.reminder-poll-delay-ms:60000}", initialDelay = 10000)
    public void deliverDueReminders() {
        if (!emailDelivery.isConfigured()) {
            if (configurationWarningShown.compareAndSet(false, true)) {
                log.warn("Reminder emails are disabled. Configure EMAIL_ENABLED and SMTP variables to deliver alerts.");
            }
            return;
        }

        for (Reminder reminder : reminders.findDueBeforeOrAt(LocalDateTime.now())) {
            EmailNotification previous = notifications.findByReminderId(reminder.id()).orElse(null);
            if (previous != null && !canAttemptAgain(previous)) continue;
            deliver(reminder, previous == null ? 0 : previous.attempts());
        }
    }

    private boolean canAttemptAgain(EmailNotification previous) {
        if (EmailNotification.SENT.equals(previous.status())) return false;
        if (!EmailNotification.FAILED.equals(previous.status())) return false;
        if (previous.attempts() >= maxAttempts) return false;
        return previous.sentAt() == null
                || !previous.sentAt().plusNanos(retryDelayMs * 1_000_000L).isAfter(LocalDateTime.now());
    }

    private void deliver(Reminder reminder, int previousAttempts) {
        Task task = tasks.findById(reminder.taskId()).orElse(null);
        if (task == null) {
            log.warn("Skipping reminder {} because task {} no longer exists", reminder.id(), reminder.taskId());
            return;
        }
        User user = users.findById(task.ownerId()).orElse(null);
        if (user == null) {
            log.warn("Skipping reminder {} because owner {} no longer exists", reminder.id(), task.ownerId());
            return;
        }

        String subject = "NEXO: recordatorio de " + task.title();
        int attempt = previousAttempts + 1;
        EmailNotification pending = new EmailNotification(null, reminder.id(), user.id(), task.id(), subject,
                EmailNotification.PENDING, attempt, LocalDateTime.now(), null);
        notifications.save(pending);
        try {
            emailDelivery.send(user.email(), subject, buildBody(task, reminder));
            notifications.save(new EmailNotification(null, reminder.id(), user.id(), task.id(), subject,
                    EmailNotification.SENT, attempt, LocalDateTime.now(), null));
            createNextOccurrence(reminder);
            log.info("reminder_email_sent reminderId={} taskId={} userId={}", reminder.id(), task.id(), user.id());
        } catch (RuntimeException ex) {
            notifications.save(new EmailNotification(null, reminder.id(), user.id(), task.id(), subject,
                    EmailNotification.FAILED, attempt, LocalDateTime.now(), compactError(ex)));
            log.error("reminder_email_failed reminderId={} taskId={} userId={}", reminder.id(), task.id(), user.id(), ex);
        }
    }

    private void createNextOccurrence(Reminder delivered) {
        if (delivered.recurrenceId() == null) return;
        recurrences.findById(delivered.recurrenceId()).ifPresent(rule -> {
            LocalDateTime nextAt = switch (rule.frequency()) {
                case DIARIA -> delivered.scheduledAt().plusDays(rule.interval());
                case SEMANAL -> delivered.scheduledAt().plusWeeks(rule.interval());
                case PERSONALIZADA -> delivered.scheduledAt().plusDays(rule.interval());
            };
            if (rule.endDate() != null && nextAt.toLocalDate().isAfter(rule.endDate())) return;

            Recurrence nextRule = recurrences.save(new Recurrence(null, rule.frequency(), rule.interval(), rule.endDate()));
            Reminder next = reminders.save(new Reminder(null, nextAt, delivered.message(), delivered.taskId(), nextRule.id()));
            log.info("recurring_reminder_generated sourceReminderId={} reminderId={}", delivered.id(), next.id());
        });
    }

    private String buildBody(Task task, Reminder reminder) {
        return "Hola,\n\nTienes un recordatorio pendiente en NEXO.\n\n"
                + "Tarea: " + task.title() + "\n"
                + "Fecha programada: " + reminder.scheduledAt() + "\n"
                + "Mensaje: " + reminder.message() + "\n\n"
                + "Ingresa a NEXO para gestionar tu tarea.";
    }

    private String compactError(Exception ex) {
        String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        return message.length() <= MAX_ERROR_DETAIL_LENGTH ? message : message.substring(0, MAX_ERROR_DETAIL_LENGTH);
    }
}
