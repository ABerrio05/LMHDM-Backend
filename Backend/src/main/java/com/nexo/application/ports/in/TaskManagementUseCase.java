package com.nexo.application.ports.in;

import com.nexo.domain.model.Task;
import com.nexo.domain.model.Recurrence;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskManagementUseCase {
    Task create(Long ownerId, CreateTaskCommand command);
    List<Task> listMine(Long ownerId);
    Task getMine(Long ownerId, Long taskId);
    Task update(Long ownerId, Long taskId, UpdateTaskCommand command);
    Task changeStatus(Long ownerId, Long taskId, Task.TaskStatus status);
    void delete(Long ownerId, Long taskId);

    record CreateTaskCommand(String title, String description, LocalDateTime dueDate, Task.Priority priority, Long spaceId,
                             Recurrence.Frequency recurrenceFrequency, Integer recurrenceInterval,
                             java.time.LocalDate recurrenceEndDate) {
        public CreateTaskCommand(String title, String description, LocalDateTime dueDate, Task.Priority priority, Long spaceId) {
            this(title, description, dueDate, priority, spaceId, null, null, null);
        }
    }
    record UpdateTaskCommand(String title, String description, LocalDateTime dueDate, Task.Priority priority) { }
}
