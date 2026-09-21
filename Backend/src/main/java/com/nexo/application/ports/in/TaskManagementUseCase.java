package com.nexo.application.ports.in;

import com.nexo.domain.model.Task;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskManagementUseCase {
    Task create(Long ownerId, CreateTaskCommand command);
    List<Task> listMine(Long ownerId);
    Task getMine(Long ownerId, Long taskId);
    Task update(Long ownerId, Long taskId, UpdateTaskCommand command);
    Task changeStatus(Long ownerId, Long taskId, Task.TaskStatus status);
    void delete(Long ownerId, Long taskId);

    record CreateTaskCommand(String title, String description, LocalDateTime dueDate, Task.Priority priority) { }
    record UpdateTaskCommand(String title, String description, LocalDateTime dueDate, Task.Priority priority) { }
}
