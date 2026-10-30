package com.nexo.application.service;

import com.nexo.application.ports.in.TaskManagementUseCase;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.application.ports.out.SpaceMemberRepositoryPort;
import com.nexo.domain.model.Task;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Casos de uso de tarea; no depende de HTTP, JPA ni Spring. */
public class TaskManagementService implements TaskManagementUseCase {
    private static final Logger log = LoggerFactory.getLogger(TaskManagementService.class);
    private final TaskRepositoryPort tasks;
    private final SpaceMemberRepositoryPort members;

    public TaskManagementService(TaskRepositoryPort tasks, SpaceMemberRepositoryPort members) {
        this.tasks = tasks; this.members = members;
    }

    @Override
    public Task create(Long ownerId, CreateTaskCommand command) {
        if (command.spaceId() != null && members.findBySpaceIdAndUserId(command.spaceId(), ownerId).isEmpty()) {
            throw new TaskAccessDeniedException();
        }
        Task created = tasks.save(new Task(null, command.title(), command.description(), command.dueDate(),
                command.priority(), Task.TaskStatus.PENDIENTE, ownerId, command.spaceId(), null));
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
        log.info("task_status_changed taskId={} ownerId={} status={}", updated.id(), ownerId, status);
        return updated;
    }

    @Override
    public void delete(Long ownerId, Long taskId) {
        tasks.delete(taskOwnedBy(ownerId, taskId));
        log.info("task_deleted taskId={} ownerId={}", taskId, ownerId);
    }

    private Task taskOwnedBy(Long ownerId, Long taskId) {
        Task task = tasks.findById(taskId).orElseThrow(TaskNotFoundException::new);
        if (!task.ownerId().equals(ownerId)) throw new TaskAccessDeniedException();
        return task;
    }

    public static class TaskNotFoundException extends RuntimeException { }
    public static class TaskAccessDeniedException extends RuntimeException { }
}
