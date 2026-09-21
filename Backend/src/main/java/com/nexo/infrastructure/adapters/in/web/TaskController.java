package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.ports.in.TaskManagementUseCase;
import com.nexo.domain.model.Task;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskManagementUseCase tasks;
    public TaskController(TaskManagementUseCase tasks) { this.tasks = tasks; }

    @PostMapping
    public ResponseEntity<Task> create(@AuthenticationPrincipal Long userId, @Valid @RequestBody TaskRequest request) {
        Task task = tasks.create(userId, new TaskManagementUseCase.CreateTaskCommand(request.title(), request.description(),
                request.dueDate(), request.priority()));
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }
    @GetMapping public List<Task> list(@AuthenticationPrincipal Long userId) { return tasks.listMine(userId); }
    @GetMapping("/{taskId}") public Task get(@AuthenticationPrincipal Long userId, @PathVariable Long taskId) { return tasks.getMine(userId, taskId); }
    @PutMapping("/{taskId}") public Task update(@AuthenticationPrincipal Long userId, @PathVariable Long taskId,
                                                   @Valid @RequestBody TaskRequest request) {
        return tasks.update(userId, taskId, new TaskManagementUseCase.UpdateTaskCommand(request.title(), request.description(),
                request.dueDate(), request.priority()));
    }
    @PatchMapping("/{taskId}/status") public Task status(@AuthenticationPrincipal Long userId, @PathVariable Long taskId,
                                                           @Valid @RequestBody StatusRequest request) {
        return tasks.changeStatus(userId, taskId, request.status());
    }
    @DeleteMapping("/{taskId}") public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long taskId) {
        tasks.delete(userId, taskId); return ResponseEntity.noContent().build();
    }

    public record TaskRequest(@NotBlank @Size(max = 255) String title, String description, LocalDateTime dueDate,
                              @NotNull Task.Priority priority) { }
    public record StatusRequest(@NotNull Task.TaskStatus status) { }
}
