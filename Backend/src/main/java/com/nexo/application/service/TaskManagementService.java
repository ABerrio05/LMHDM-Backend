package com.nexo.application.service;

import com.nexo.application.ports.in.TaskManagementUseCase;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.domain.model.Task;
import java.util.List;

/** Casos de uso de tarea; no depende de HTTP, JPA ni Spring. */
public class TaskManagementService implements TaskManagementUseCase {
    private final TaskRepositoryPort tasks;

    public TaskManagementService(TaskRepositoryPort tasks) { this.tasks = tasks; }

    @Override
    public Task create(Long ownerId, CreateTaskCommand command) {
        return tasks.save(new Task(null, command.title(), command.description(), command.dueDate(),
                command.priority(), Task.TaskStatus.PENDIENTE, ownerId, null, null));
    }

    @Override
    public List<Task> listMine(Long ownerId) { return tasks.findAllByOwnerId(ownerId); }

    @Override
    public Task getMine(Long ownerId, Long taskId) { return taskOwnedBy(ownerId, taskId); }

    @Override
    public Task update(Long ownerId, Long taskId, UpdateTaskCommand command) {
        Task current = taskOwnedBy(ownerId, taskId);
        return tasks.save(new Task(current.id(), command.title(), command.description(), command.dueDate(),
                command.priority(), current.status(), current.ownerId(), current.spaceId(), current.recurrenceId()));
    }

    @Override
    public Task changeStatus(Long ownerId, Long taskId, Task.TaskStatus status) {
        Task current = taskOwnedBy(ownerId, taskId);
        return tasks.save(new Task(current.id(), current.title(), current.description(), current.dueDate(),
                current.priority(), status, current.ownerId(), current.spaceId(), current.recurrenceId()));
    }

    @Override
    public void delete(Long ownerId, Long taskId) { tasks.delete(taskOwnedBy(ownerId, taskId)); }

    private Task taskOwnedBy(Long ownerId, Long taskId) {
        Task task = tasks.findById(taskId).orElseThrow(TaskNotFoundException::new);
        if (!task.ownerId().equals(ownerId)) throw new TaskAccessDeniedException();
        return task;
    }

    public static class TaskNotFoundException extends RuntimeException { }
    public static class TaskAccessDeniedException extends RuntimeException { }
}
