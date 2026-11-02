package com.nexo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nexo.application.ports.in.TaskManagementUseCase.CreateTaskCommand;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.application.ports.out.SpaceMemberRepositoryPort;
import com.nexo.application.ports.out.RecurrenceRepositoryPort;
import com.nexo.domain.model.Recurrence;
import com.nexo.domain.model.SpaceMember;
import com.nexo.domain.model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TaskManagementServiceTest {
    @Test
    void createsPendingTaskForAuthenticatedOwner() {
        List<Task> saved = new ArrayList<>();
        TaskRepositoryPort repository = new InMemoryTaskRepository(saved);
        SpaceMemberRepositoryPort members = new InMemoryMemberRepository();
        TaskManagementService service = new TaskManagementService(repository, members, new InMemoryRecurrenceRepository());

        Task result = service.create(7L, new CreateTaskCommand("Preparar comité", "Diapositivas", null, Task.Priority.ALTA, null));

        assertEquals(7L, result.ownerId());
        assertEquals(Task.TaskStatus.PENDIENTE, result.status());
    }

    @Test
    void rejectsCreationInSpaceWhenUserIsNotAMember() {
        TaskRepositoryPort repository = new InMemoryTaskRepository(new ArrayList<>());
        SpaceMemberRepositoryPort members = new InMemoryMemberRepository();
        TaskManagementService service = new TaskManagementService(repository, members, new InMemoryRecurrenceRepository());

        assertThrows(TaskManagementService.TaskAccessDeniedException.class,
                () -> service.create(7L, new CreateTaskCommand("Privada", "", null, Task.Priority.MEDIA, 15L)));
    }

    @Test
    void preventsAnotherUserFromReadingOrDeletingATask() {
        List<Task> saved = new ArrayList<>();
        TaskManagementService service = new TaskManagementService(new InMemoryTaskRepository(saved), new InMemoryMemberRepository(), new InMemoryRecurrenceRepository());
        Task created = service.create(7L, new CreateTaskCommand("Propia", "", null, Task.Priority.BAJA, null));

        assertThrows(TaskManagementService.TaskAccessDeniedException.class, () -> service.getMine(8L, created.id()));
        assertThrows(TaskManagementService.TaskAccessDeniedException.class, () -> service.delete(8L, created.id()));
    }

    @Test
    void changesOnlyTheStatusOfAnOwnedTask() {
        List<Task> saved = new ArrayList<>();
        TaskManagementService service = new TaskManagementService(new InMemoryTaskRepository(saved), new InMemoryMemberRepository(), new InMemoryRecurrenceRepository());
        Task created = service.create(7L, new CreateTaskCommand("Preparar pruebas", "", null, Task.Priority.ALTA, null));

        Task completed = service.changeStatus(7L, created.id(), Task.TaskStatus.COMPLETADA);

        assertEquals(Task.TaskStatus.COMPLETADA, completed.status());
        assertEquals("Preparar pruebas", completed.title());
    }

    @Test
    void completingRecurringTaskCreatesTheNextOccurrence() {
        List<Task> saved = new ArrayList<>();
        InMemoryTaskRepository taskRepository = new InMemoryTaskRepository(saved);
        TaskManagementService service = new TaskManagementService(taskRepository, new InMemoryMemberRepository(), new InMemoryRecurrenceRepository());
        LocalDateTime dueDate = LocalDateTime.of(2030, 1, 10, 9, 0);
        Task created = service.create(7L, new CreateTaskCommand("Informe semanal", "", dueDate, Task.Priority.MEDIA, null,
                Recurrence.Frequency.SEMANAL, 1, null));

        service.changeStatus(7L, created.id(), Task.TaskStatus.COMPLETADA);

        assertEquals(2, saved.size());
        Task next = saved.stream().filter(task -> !task.id().equals(created.id())).findFirst().orElseThrow();
        assertEquals(dueDate.plusWeeks(1), next.dueDate());
        assertEquals(Task.TaskStatus.PENDIENTE, next.status());
        assertEquals(created.title(), next.title());
    }

    private static class InMemoryTaskRepository implements TaskRepositoryPort {
        private final List<Task> tasks;
        private InMemoryTaskRepository(List<Task> tasks) { this.tasks = tasks; }
        public Task save(Task task) {
            long id = task.id() == null ? tasks.stream().mapToLong(Task::id).max().orElse(0L) + 1 : task.id();
            Task saved = new Task(id, task.title(), task.description(), task.dueDate(),
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
        public void deleteAllBySpaceId(Long spaceId) { }
    }

    private static class InMemoryRecurrenceRepository implements RecurrenceRepositoryPort {
        private final List<Recurrence> recurrences = new ArrayList<>();
        public Recurrence save(Recurrence recurrence) {
            Recurrence saved = new Recurrence(recurrence.id() == null ? (long) recurrences.size() + 1 : recurrence.id(),
                    recurrence.frequency(), recurrence.interval(), recurrence.endDate());
            recurrences.removeIf(current -> current.id().equals(saved.id())); recurrences.add(saved); return saved;
        }
        public Optional<Recurrence> findById(Long recurrenceId) { return recurrences.stream().filter(item -> item.id().equals(recurrenceId)).findFirst(); }
        public void deleteById(Long recurrenceId) { recurrences.removeIf(item -> item.id().equals(recurrenceId)); }
    }
}
