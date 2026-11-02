package com.nexo.application.service;

import com.nexo.application.ports.in.TaskManagementUseCase;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.application.ports.out.RecurrenceRepositoryPort;
import com.nexo.application.ports.out.SpaceMemberRepositoryPort;
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

    public TaskManagementService(TaskRepositoryPort tasks, SpaceMemberRepositoryPort members, RecurrenceRepositoryPort recurrences) {
        this.tasks = tasks; this.members = members; this.recurrences = recurrences;
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
        return created;
    }

    @Override
    public List<Task> listMine(Long ownerId) { return tasks.findAllByOwnerId(ownerId); }

    @Override
    public Task getMine(Long ownerId, Long taskId) { return taskOwnedBy(ownerId, taskId); }

    @Override
    public Task update(Long ownerId, Long taskId, UpdateTaskCommand command) {
        Task current = taskOwnedBy(ownerId, taskId);
        Task updated = tasks.save(new Task(current.id(), command.title(), command.description(), command.dueDate(),
                command.priority(), current.status(), current.ownerId(), current.spaceId(), current.recurrenceId()));
        log.info("task_updated taskId={} ownerId={}", updated.id(), ownerId);
        return updated;
    }

    @Override
    public Task changeStatus(Long ownerId, Long taskId, Task.TaskStatus status) {
        Task current = taskOwnedBy(ownerId, taskId);
        Task updated = tasks.save(new Task(current.id(), current.title(), current.description(), current.dueDate(),
                current.priority(), status, current.ownerId(), current.spaceId(), current.recurrenceId()));
        if (status == Task.TaskStatus.COMPLETADA && current.status() != Task.TaskStatus.COMPLETADA) {
            createNextOccurrence(current);
        }
        log.info("task_status_changed taskId={} ownerId={} status={}", updated.id(), ownerId, status);
        return updated;
    }

    @Override
    public void delete(Long ownerId, Long taskId) {
        Task task = taskOwnedBy(ownerId, taskId);
        tasks.delete(task);
        if (task.recurrenceId() != null) recurrences.deleteById(task.recurrenceId());
        log.info("task_deleted taskId={} ownerId={}", taskId, ownerId);
    }

    private Task taskOwnedBy(Long ownerId, Long taskId) {
        Task task = tasks.findById(taskId).orElseThrow(TaskNotFoundException::new);
        if (!task.ownerId().equals(ownerId)) throw new TaskAccessDeniedException();
        return task;
    }

    private Long createRecurrence(CreateTaskCommand command) {
        if (command.recurrenceFrequency() == null) return null;
        if (command.dueDate() == null) throw new IllegalArgumentException("Una tarea recurrente requiere fecha límite");
        int interval = command.recurrenceInterval() == null ? 1 : command.recurrenceInterval();
        return recurrences.save(new Recurrence(null, command.recurrenceFrequency(), interval,
                command.recurrenceEndDate())).id();
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
