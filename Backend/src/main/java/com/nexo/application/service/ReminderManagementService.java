package com.nexo.application.service;

import com.nexo.application.ports.in.ReminderManagementUseCase;
import com.nexo.application.ports.out.ReminderRepositoryPort;
import com.nexo.application.ports.out.EmailNotificationRepositoryPort;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.application.ports.out.RecurrenceRepositoryPort;
import com.nexo.application.ports.out.SpaceMemberRepositoryPort;
import com.nexo.domain.model.Reminder;
import com.nexo.domain.model.Recurrence;
import com.nexo.domain.model.Task;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReminderManagementService implements ReminderManagementUseCase {
    private static final Logger log = LoggerFactory.getLogger(ReminderManagementService.class);
    private final ReminderRepositoryPort reminders;
    private final TaskRepositoryPort tasks;
    private final EmailNotificationRepositoryPort notifications;
    private final RecurrenceRepositoryPort recurrences;
    private final SpaceMemberRepositoryPort members;
    public ReminderManagementService(ReminderRepositoryPort reminders, TaskRepositoryPort tasks) {
        this(reminders, tasks, new NoOpNotificationRepository(), new NoOpRecurrenceRepository(), new NoOpSpaceMemberRepository());
    }
    public ReminderManagementService(ReminderRepositoryPort reminders, TaskRepositoryPort tasks,
                                    EmailNotificationRepositoryPort notifications) {
        this(reminders, tasks, notifications, new NoOpRecurrenceRepository(), new NoOpSpaceMemberRepository());
    }
    public ReminderManagementService(ReminderRepositoryPort reminders, TaskRepositoryPort tasks,
                                    EmailNotificationRepositoryPort notifications, RecurrenceRepositoryPort recurrences) {
        this(reminders, tasks, notifications, recurrences, new NoOpSpaceMemberRepository());
    }
    public ReminderManagementService(ReminderRepositoryPort reminders, TaskRepositoryPort tasks,
                                    EmailNotificationRepositoryPort notifications, RecurrenceRepositoryPort recurrences,
                                    SpaceMemberRepositoryPort members) {
        this.reminders = reminders; this.tasks = tasks; this.notifications = notifications; this.recurrences = recurrences;
        this.members = members;
    }
    @Override public Reminder create(Long ownerId, Long taskId, CreateReminderCommand command) {
        taskManagedBy(ownerId, taskId);
        Long recurrenceId = createRecurrence(command);
        Reminder created = reminders.save(new Reminder(null, command.scheduledAt(), command.message(), taskId, recurrenceId));
        log.info("reminder_created reminderId={} taskId={} ownerId={}", created.id(), taskId, ownerId);
        return created;
    }
    @Override public List<Reminder> listForTask(Long ownerId, Long taskId) {
        taskManagedBy(ownerId, taskId); return reminders.findAllByTaskId(taskId);
    }
    @Override public Reminder update(Long ownerId, Long reminderId, UpdateReminderCommand command) {
        Reminder current = reminders.findById(reminderId).orElseThrow(ReminderNotFoundException::new);
        taskManagedBy(ownerId, current.taskId());
        notifications.deleteByReminderId(reminderId);
        Reminder updated = reminders.save(new Reminder(current.id(), command.scheduledAt(), command.message(), current.taskId(), current.recurrenceId()));
        log.info("reminder_updated reminderId={} ownerId={}", updated.id(), ownerId);
        return updated;
    }
    @Override public void delete(Long ownerId, Long reminderId) {
        Reminder reminder = reminders.findById(reminderId).orElseThrow(ReminderNotFoundException::new);
        taskManagedBy(ownerId, reminder.taskId());
        notifications.deleteByReminderId(reminderId);
        reminders.delete(reminder);
        if (reminder.recurrenceId() != null) recurrences.deleteById(reminder.recurrenceId());
        log.info("reminder_deleted reminderId={} ownerId={}", reminderId, ownerId);
    }
    private void taskOwnedBy(Long ownerId, Long taskId) {
        Task task = tasks.findById(taskId).orElseThrow(TaskManagementService.TaskNotFoundException::new);
        if (!task.ownerId().equals(ownerId)) throw new TaskManagementService.TaskAccessDeniedException();
    }
    private void taskManagedBy(Long userId, Long taskId) {
        Task task = tasks.findById(taskId).orElseThrow(TaskManagementService.TaskNotFoundException::new);
        if (task.spaceId() == null) {
            taskOwnedBy(userId, taskId);
            return;
        }
        boolean allowed = members.findBySpaceIdAndUserId(task.spaceId(), userId)
                .map(member -> member.role() != com.nexo.domain.model.SpaceMember.Role.INVITADO).orElse(false);
        if (!allowed) throw new TaskManagementService.TaskAccessDeniedException();
    }
    private Long createRecurrence(CreateReminderCommand command) {
        if (command.recurrenceFrequency() == null) return null;
        int interval = command.recurrenceInterval() == null ? 1 : command.recurrenceInterval();
        return recurrences.save(new Recurrence(null, command.recurrenceFrequency(), interval,
                command.recurrenceEndDate())).id();
    }
    public static class ReminderNotFoundException extends RuntimeException { }

    private static final class NoOpNotificationRepository implements EmailNotificationRepositoryPort {
        @Override public java.util.Optional<com.nexo.domain.model.EmailNotification> findByReminderId(Long reminderId) {
            return java.util.Optional.empty();
        }
        @Override public com.nexo.domain.model.EmailNotification save(com.nexo.domain.model.EmailNotification notification) {
            return notification;
        }
        @Override public void deleteByReminderId(Long reminderId) { }
    }
    private static final class NoOpRecurrenceRepository implements RecurrenceRepositoryPort {
        @Override public Recurrence save(Recurrence recurrence) { return new Recurrence(1L, recurrence.frequency(), recurrence.interval(), recurrence.endDate()); }
        @Override public java.util.Optional<Recurrence> findById(Long recurrenceId) { return java.util.Optional.empty(); }
        @Override public void deleteById(Long recurrenceId) { }
    }
    private static final class NoOpSpaceMemberRepository implements SpaceMemberRepositoryPort {
        @Override public com.nexo.domain.model.SpaceMember save(com.nexo.domain.model.SpaceMember member) { return member; }
        @Override public java.util.Optional<com.nexo.domain.model.SpaceMember> findBySpaceIdAndUserId(Long spaceId, Long userId) { return java.util.Optional.empty(); }
        @Override public List<com.nexo.domain.model.SpaceMember> findAllBySpaceId(Long spaceId) { return List.of(); }
        @Override public List<com.nexo.domain.model.SpaceMember> findAllByUserId(Long userId) { return List.of(); }
        @Override public com.nexo.domain.model.SpaceMember updateRole(Long spaceId, Long userId, com.nexo.domain.model.SpaceMember.Role role) { throw new UnsupportedOperationException(); }
        @Override public void deleteAllBySpaceId(Long spaceId) { }
    }
}
