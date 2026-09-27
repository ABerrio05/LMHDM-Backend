package com.nexo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nexo.application.ports.in.TaskManagementUseCase.CreateTaskCommand;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.application.ports.out.SpaceMemberRepositoryPort;
import com.nexo.domain.model.SpaceMember;
import com.nexo.domain.model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TaskManagementServiceTest {
    @Test
    void createsPendingTaskForAuthenticatedOwner() {
        List<Task> saved = new ArrayList<>();
        TaskRepositoryPort repository = new InMemoryTaskRepository(saved);
        SpaceMemberRepositoryPort members = new InMemoryMemberRepository();
        TaskManagementService service = new TaskManagementService(repository, members);

        Task result = service.create(7L, new CreateTaskCommand("Preparar comité", "Diapositivas", null, Task.Priority.ALTA, null));

        assertEquals(7L, result.ownerId());
        assertEquals(Task.TaskStatus.PENDIENTE, result.status());
    }

    private static class InMemoryTaskRepository implements TaskRepositoryPort {
        private final List<Task> tasks;
        private InMemoryTaskRepository(List<Task> tasks) { this.tasks = tasks; }
        public Task save(Task task) {
            Task saved = new Task(task.id() == null ? 1L : task.id(), task.title(), task.description(), task.dueDate(),
                    task.priority(), task.status(), task.ownerId(), task.spaceId(), task.recurrenceId());
            tasks.removeIf(current -> current.id().equals(saved.id())); tasks.add(saved); return saved;
        }
        public List<Task> findAllByOwnerId(Long ownerId) { return tasks.stream().filter(task -> task.ownerId().equals(ownerId)).toList(); }
        public List<Task> findAllBySpaceId(Long spaceId) { return tasks.stream().filter(task -> spaceId.equals(task.spaceId())).toList(); }
        public Optional<Task> findById(Long taskId) { return tasks.stream().filter(task -> task.id().equals(taskId)).findFirst(); }
        public void delete(Task task) { tasks.removeIf(current -> current.id().equals(task.id())); }
    }

    private static class InMemoryMemberRepository implements SpaceMemberRepositoryPort {
        public SpaceMember save(SpaceMember member) { return member; }
        public Optional<SpaceMember> findBySpaceIdAndUserId(Long spaceId, Long userId) { return Optional.empty(); }
        public List<SpaceMember> findAllBySpaceId(Long spaceId) { return List.of(); }
        public List<SpaceMember> findAllByUserId(Long userId) { return List.of(); }
    }
}
