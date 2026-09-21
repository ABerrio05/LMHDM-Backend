package com.nexo.application.service;

import com.nexo.application.ports.in.ReminderManagementUseCase;
import com.nexo.application.ports.out.ReminderRepositoryPort;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.domain.model.Reminder;
import com.nexo.domain.model.Task;
import java.util.List;

public class ReminderManagementService implements ReminderManagementUseCase {
    private final ReminderRepositoryPort reminders;
    private final TaskRepositoryPort tasks;
    public ReminderManagementService(ReminderRepositoryPort reminders, TaskRepositoryPort tasks) {
        this.reminders = reminders; this.tasks = tasks;
    }
    @Override public Reminder create(Long ownerId, Long taskId, CreateReminderCommand command) {
        taskOwnedBy(ownerId, taskId);
        return reminders.save(new Reminder(null, command.scheduledAt(), command.message(), taskId));
    }
    @Override public List<Reminder> listForTask(Long ownerId, Long taskId) {
        taskOwnedBy(ownerId, taskId); return reminders.findAllByTaskId(taskId);
    }
    @Override public void delete(Long ownerId, Long reminderId) {
        Reminder reminder = reminders.findById(reminderId).orElseThrow(ReminderNotFoundException::new);
        taskOwnedBy(ownerId, reminder.taskId()); reminders.delete(reminder);
    }
    private void taskOwnedBy(Long ownerId, Long taskId) {
        Task task = tasks.findById(taskId).orElseThrow(TaskManagementService.TaskNotFoundException::new);
        if (!task.ownerId().equals(ownerId)) throw new TaskManagementService.TaskAccessDeniedException();
    }
    public static class ReminderNotFoundException extends RuntimeException { }
}
