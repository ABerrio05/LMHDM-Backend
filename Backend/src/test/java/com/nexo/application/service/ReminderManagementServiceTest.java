package com.nexo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nexo.application.ports.in.ReminderManagementUseCase.CreateReminderCommand;
import com.nexo.application.ports.in.ReminderManagementUseCase.UpdateReminderCommand;
import com.nexo.application.ports.out.ReminderRepositoryPort;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.domain.model.Reminder;
import com.nexo.domain.model.Task;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReminderManagementServiceTest {
    private InMemoryReminderRepository reminders;
    private ReminderManagementService service;
    private final LocalDateTime scheduledAt = LocalDateTime.of(2026, 10, 30, 9, 0);

    @BeforeEach
    void setUp() {
        reminders = new InMemoryReminderRepository();
        Task ownedTask = new Task(10L, "Preparar revisión", "", null, Task.Priority.ALTA,
                Task.TaskStatus.PENDIENTE, 7L, null, null);
        service = new ReminderManagementService(reminders, new SingleTaskRepository(ownedTask));
    }

    @Test
    void createsReminderOnlyForTaskOwner() {
        Reminder created = service.create(7L, 10L, new CreateReminderCommand(scheduledAt, "Revisar pruebas"));

        assertEquals(1L, created.id());
        assertEquals(10L, created.taskId());
        assertEquals("Revisar pruebas", created.message());
    }

    @Test
    void rejectsReminderCreationForAnotherUser() {
        assertThrows(TaskManagementService.TaskAccessDeniedException.class,
                () -> service.create(8L, 10L, new CreateReminderCommand(scheduledAt, "No permitido")));
    }

    @Test
    void updatesAndDeletesExistingOwnedReminder() {
        Reminder created = service.create(7L, 10L, new CreateReminderCommand(scheduledAt, "Inicial"));

        Reminder updated = service.update(7L, created.id(), new UpdateReminderCommand(scheduledAt.plusDays(1), "Actualizado"));
        service.delete(7L, created.id());

        assertEquals("Actualizado", updated.message());
        assertEquals(0, reminders.count());
    }

    @Test
    void reportsMissingReminder() {
        assertThrows(ReminderManagementService.ReminderNotFoundException.class,
                () -> service.update(7L, 999L, new UpdateReminderCommand(scheduledAt, "No existe")));
    }

    private static final class SingleTaskRepository implements TaskRepositoryPort {
        private final Task task;

        private SingleTaskRepository(Task task) {
            this.task = task;
        }

        @Override public Task save(Task value) { return value; }
        @Override public List<Task> findAllByOwnerId(Long ownerId) { return ownerId.equals(task.ownerId()) ? List.of(task) : List.of(); }
        @Override public List<Task> findAllBySpaceId(Long spaceId) { return List.of(); }
        @Override public Optional<Task> findById(Long taskId) { return task.id().equals(taskId) ? Optional.of(task) : Optional.empty(); }
        @Override public void delete(Task value) { }
    }

    private static final class InMemoryReminderRepository implements ReminderRepositoryPort {
        private final List<Reminder> data = new ArrayList<>();

        @Override
        public Reminder save(Reminder reminder) {
            Reminder saved = new Reminder(reminder.id() == null ? 1L : reminder.id(), reminder.scheduledAt(),
                    reminder.message(), reminder.taskId());
            data.removeIf(current -> current.id().equals(saved.id()));
            data.add(saved);
            return saved;
        }

        @Override public List<Reminder> findAllByTaskId(Long taskId) { return data.stream().filter(item -> item.taskId().equals(taskId)).toList(); }
        @Override public Optional<Reminder> findById(Long reminderId) { return data.stream().filter(item -> item.id().equals(reminderId)).findFirst(); }
        @Override public void delete(Reminder reminder) { data.removeIf(item -> item.id().equals(reminder.id())); }
        int count() { return data.size(); }
    }
}
