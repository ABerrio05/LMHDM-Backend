package com.nexo.application.service;

import com.nexo.application.ports.in.TaskManagementUseCase;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.application.ports.out.RecurrenceRepositoryPort;
import com.nexo.application.ports.out.ReminderRepositoryPort;
import com.nexo.application.ports.out.UserRepositoryPort;
import com.nexo.application.ports.out.SpaceMemberRepositoryPort;
import com.nexo.application.ports.out.EmailNotificationRepositoryPort;
import com.nexo.domain.model.Task;
import com.nexo.domain.model.Recurrence;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Casos de uso de tarea; no depende de HTTP, JPA ni Spring. */
public class TaskManagementService implements TaskManagementUseCase {
    private static final Logger log = LoggerFactory.getLogger(TaskManagementService.class);
    private final TaskRepositoryPort tasks;
    private final SpaceMemberRepositoryPort members;
    private final RecurrenceRepositoryPort recurrences;
    private final ReminderRepositoryPort reminders;
    private final UserRepositoryPort users;
    private final ActivityEmailNotificationService activityEmails;
    private final EmailNotificationRepositoryPort notifications;

    public TaskManagementService(TaskRepositoryPort tasks, SpaceMemberRepositoryPort members, RecurrenceRepositoryPort recurrences) {
        this(tasks, members, recurrences, null, null, null, null);
    }

    public TaskManagementService(TaskRepositoryPort tasks, SpaceMemberRepositoryPort members,
                                 RecurrenceRepositoryPort recurrences, ReminderRepositoryPort reminders) {
        this(tasks, members, recurrences, reminders, null, null, null);
    }

    public TaskManagementService(TaskRepositoryPort tasks, SpaceMemberRepositoryPort members,
                                 RecurrenceRepositoryPort recurrences, ReminderRepositoryPort reminders,
                                 UserRepositoryPort users, ActivityEmailNotificationService activityEmails) {
        this(tasks, members, recurrences, reminders, users, activityEmails, null);
    }

    public TaskManagementService(TaskRepositoryPort tasks, SpaceMemberRepositoryPort members,
                                 RecurrenceRepositoryPort recurrences, ReminderRepositoryPort reminders,
                                 UserRepositoryPort users, ActivityEmailNotificationService activityEmails,
                                 EmailNotificationRepositoryPort notifications) {
        this.tasks = tasks; this.members = members; this.recurrences = recurrences; this.reminders = reminders;
        this.users = users; this.activityEmails = activityEmails; this.notifications = notifications;
    }

    @Override
    public Task create(Long ownerId, CreateTaskCommand command) {
        if (command.spaceId() != null && members.findBySpaceIdAndUserId(command.spaceId(), ownerId).isEmpty()) {
            throw new TaskAccessDeniedException();
        }
        Long recurrenceId = createRecurrence(command);
        Task created = tasks.save(new Task(null, command.title(), command.description(), command.dueDate(),
                command.priority(), Task.TaskStatus.PENDIENTE, ownerId, command.spaceId(), recurrenceId));
        log.info("task_created taskId={} ownerId={} spaceId={} priority={}", created.id(), ownerId,
                created.spaceId(), created.priority());
        if (users != null && activityEmails != null) {
            users.findById(ownerId).ifPresent(owner -> {
                activityEmails.taskCreated(owner, created);
                notifySpaceMembers(created, owner, false);
            });
        }
        return created;
    }

    @Override
    public List<Task> listMine(Long ownerId) { return tasks.findAllByOwnerId(ownerId); }

    @Override
    public Task getMine(Long ownerId, Long taskId) { return taskAccessibleBy(ownerId, taskId); }

    @Override
    public Task update(Long ownerId, Long taskId, UpdateTaskCommand command) {
        Task current = taskEditableBy(ownerId, taskId);
        Task updated = tasks.save(new Task(current.id(), command.title(), command.description(), command.dueDate(),
                command.priority(), current.status(), current.ownerId(), current.spaceId(), current.recurrenceId()));
        log.info("task_updated taskId={} ownerId={}", updated.id(), ownerId);
        if (users != null && activityEmails != null) users.findById(ownerId).ifPresent(owner -> {
            activityEmails.taskUpdated(owner, updated);
            notifySpaceMembers(updated, owner, "updated");
        });
        return updated;
    }

    @Override
    public Task changeStatus(Long ownerId, Long taskId, Task.TaskStatus status) {
        Task current = taskEditableBy(ownerId, taskId);
        Task updated = tasks.save(new Task(current.id(), current.title(), current.description(), current.dueDate(),
                current.priority(), status, current.ownerId(), current.spaceId(), current.recurrenceId()));
        if (status == Task.TaskStatus.COMPLETADA && current.status() != Task.TaskStatus.COMPLETADA) {
            createNextOccurrence(current);
            if (users != null && activityEmails != null) {
                users.findById(ownerId).ifPresent(owner -> notifySpaceMembers(updated, owner, true));
            }
        }
        log.info("task_status_changed taskId={} ownerId={} status={}", updated.id(), ownerId, status);
        return updated;
    }

    @Override
    public void delete(Long ownerId, Long taskId) {
        Task task = taskEditableBy(ownerId, taskId);
        if (reminders != null) reminders.findAllByTaskId(taskId).forEach(reminder -> {
            if (notifications != null) notifications.deleteByReminderId(reminder.id());
            reminders.delete(reminder);
            if (reminder.recurrenceId() != null) recurrences.deleteById(reminder.recurrenceId());
        });
        tasks.delete(task);
        if (task.recurrenceId() != null) recurrences.deleteById(task.recurrenceId());
        log.info("task_deleted taskId={} ownerId={}", taskId, ownerId);
        if (users != null && activityEmails != null) users.findById(ownerId).ifPresent(owner -> {
            activityEmails.taskDeleted(owner, task);
            notifySpaceMembers(task, owner, "deleted");
        });
    }

    private Task taskOwnedBy(Long ownerId, Long taskId) {
        Task task = tasks.findById(taskId).orElseThrow(TaskNotFoundException::new);
        if (!task.ownerId().equals(ownerId)) throw new TaskAccessDeniedException();
        return task;
    }

    private Task taskAccessibleBy(Long userId, Long taskId) {
        Task task = tasks.findById(taskId).orElseThrow(TaskNotFoundException::new);
        if (task.spaceId() == null) return taskOwnedBy(userId, taskId);
        if (members.findBySpaceIdAndUserId(task.spaceId(), userId).isEmpty()) throw new TaskAccessDeniedException();
        return task;
    }

    /** En espacios compartidos, todos salvo los invitados pueden gestionar tareas. */
    private Task taskEditableBy(Long userId, Long taskId) {
        Task task = taskAccessibleBy(userId, taskId);
        if (task.spaceId() != null && members.findBySpaceIdAndUserId(task.spaceId(), userId)
                .map(member -> member.role() == com.nexo.domain.model.SpaceMember.Role.INVITADO).orElse(true)) {
            throw new TaskAccessDeniedException();
        }
        return task;
    }

    private Long createRecurrence(CreateTaskCommand command) {
        if (command.recurrenceFrequency() == null) return null;
        if (command.dueDate() == null) throw new IllegalArgumentException("Una tarea recurrente requiere fecha límite");
        int interval = command.recurrenceInterval() == null ? 1 : command.recurrenceInterval();
        return recurrences.save(new Recurrence(null, command.recurrenceFrequency(), interval,
                command.recurrenceEndDate())).id();
    }

    private void notifySpaceMembers(Task task, com.nexo.domain.model.User actor, boolean completed) {
        notifySpaceMembers(task, actor, completed ? "completed" : "created");
    }

    private void notifySpaceMembers(Task task, com.nexo.domain.model.User actor, String event) {
        if (task.spaceId() == null) return;
        members.findAllBySpaceId(task.spaceId()).stream()
                .map(member -> member.userId())
                .filter(memberId -> !memberId.equals(actor.id()))
                .distinct()
                .forEach(memberId -> users.findById(memberId).ifPresent(recipient -> {
                    switch (event) {
                        case "completed" -> activityEmails.sharedTaskCompleted(recipient, actor, task);
                        case "updated" -> activityEmails.sharedTaskUpdated(recipient, actor, task);
                        case "deleted" -> activityEmails.sharedTaskDeleted(recipient, actor, task);
                        default -> activityEmails.sharedTaskCreated(recipient, actor, task);
                    }
                }));
    }

    /** Al completar una tarea recurrente, crea la próxima sin modificar el historial. */
    private void createNextOccurrence(Task completed) {
        if (completed.recurrenceId() == null || completed.dueDate() == null) return;
        recurrences.findById(completed.recurrenceId()).ifPresent(rule -> {
            LocalDateTime nextDueDate = switch (rule.frequency()) {
                case DIARIA -> completed.dueDate().plusDays(rule.interval());
                case SEMANAL -> completed.dueDate().plusWeeks(rule.interval());
                case PERSONALIZADA -> completed.dueDate().plusDays(rule.interval());
            };
            if (rule.endDate() != null && nextDueDate.toLocalDate().isAfter(rule.endDate())) return;
            Long nextRuleId = recurrences.save(new Recurrence(null, rule.frequency(), rule.interval(), rule.endDate())).id();
            Task next = tasks.save(new Task(null, completed.title(), completed.description(), nextDueDate,
                    completed.priority(), Task.TaskStatus.PENDIENTE, completed.ownerId(), completed.spaceId(), nextRuleId));
            log.info("recurring_task_generated completedTaskId={} nextTaskId={} ownerId={}", completed.id(), next.id(),
                    completed.ownerId());
        });
    }

    public static class TaskNotFoundException extends RuntimeException { }
    public static class TaskAccessDeniedException extends RuntimeException { }
}
