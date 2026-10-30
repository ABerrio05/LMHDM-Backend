package com.nexo.application.service;

import com.nexo.application.ports.in.ReminderManagementUseCase;
import com.nexo.application.ports.out.ReminderRepositoryPort;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.domain.model.Reminder;
import com.nexo.domain.model.Task;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReminderManagementService implements ReminderManagementUseCase {
    private static final Logger log = LoggerFactory.getLogger(ReminderManagementService.class);
    private final ReminderRepositoryPort reminders;
    private final TaskRepositoryPort tasks;
    public ReminderManagementService(ReminderRepositoryPort reminders, TaskRepositoryPort tasks) {
        this.reminders = reminders; this.tasks = tasks;
    }
    @Override public Reminder create(Long ownerId, Long taskId, CreateReminderCommand command) {
        taskOwnedBy(ownerId, taskId);
        Reminder created = reminders.save(new Reminder(null, command.scheduledAt(), command.message(), taskId));
        log.info("reminder_created reminderId={} taskId={} ownerId={}", created.id(), taskId, ownerId);
        return created;
    }
    @Override public List<Reminder> listForTask(Long ownerId, Long taskId) {
        taskOwnedBy(ownerId, taskId); return reminders.findAllByTaskId(taskId);
    }
    @Override public Reminder update(Long ownerId, Long reminderId, UpdateReminderCommand command) {
        Reminder current = reminders.findById(reminderId).orElseThrow(ReminderNotFoundException::new);
        taskOwnedBy(ownerId, current.taskId());
        Reminder updated = reminders.save(new Reminder(current.id(), command.scheduledAt(), command.message(), current.taskId()));
        log.info("reminder_updated reminderId={} ownerId={}", updated.id(), ownerId);
        return updated;
    }
    @Override public void delete(Long ownerId, Long reminderId) {
        Reminder reminder = reminders.findById(reminderId).orElseThrow(ReminderNotFoundException::new);
        taskOwnedBy(ownerId, reminder.taskId()); reminders.delete(reminder);
        log.info("reminder_deleted reminderId={} ownerId={}", reminderId, ownerId);
    }
    private void taskOwnedBy(Long ownerId, Long taskId) {
        Task task = tasks.findById(taskId).orElseThrow(TaskManagementService.TaskNotFoundException::new);
        if (!task.ownerId().equals(ownerId)) throw new TaskManagementService.TaskAccessDeniedException();
    }
    public static class ReminderNotFoundException extends RuntimeException { }
}
