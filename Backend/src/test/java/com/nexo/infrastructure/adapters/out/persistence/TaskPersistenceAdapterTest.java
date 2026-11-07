package com.nexo.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexo.domain.model.Task;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TaskPersistenceAdapterTest {

    @Mock
    private SpringDataTaskRepository repository;

    private TaskPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new TaskPersistenceAdapter(repository);
    }

    @Test
    void savesAndMapsANewTask() {
        LocalDateTime dueDate = LocalDateTime.of(2026, 11, 12, 10, 30);
        Task task = new Task(null, "Presentación", "Preparar demo", dueDate,
                Task.Priority.ALTA, Task.TaskStatus.PENDIENTE, 7L, 3L, 2L);

        when(repository.save(any(TaskJpaEntity.class))).thenAnswer(invocation -> {
            TaskJpaEntity saved = invocation.getArgument(0);
            setId(saved, 15L);
            return saved;
        });

        Task result = adapter.save(task);

        ArgumentCaptor<TaskJpaEntity> captor = ArgumentCaptor.forClass(TaskJpaEntity.class);
        verify(repository).save(captor.capture());
        TaskJpaEntity persisted = captor.getValue();
        assertEquals("alta", persisted.getPriority());
        assertEquals("pendiente", persisted.getStatus());
        assertEquals(15L, result.id());
        assertEquals(task.title(), result.title());
        assertEquals(task.dueDate(), result.dueDate());
        assertEquals(Task.Priority.ALTA, result.priority());
        assertEquals(Task.TaskStatus.PENDIENTE, result.status());
    }

    @Test
    void updatesAnExistingTask() {
        LocalDateTime originalDate = LocalDateTime.of(2026, 11, 10, 8, 0);
        TaskJpaEntity existing = new TaskJpaEntity("Anterior", "Descripción", originalDate,
                "baja", "pendiente", 1L, null, null);
        setId(existing, 9L);
        LocalDateTime newDate = originalDate.plusDays(2);
        Task updated = new Task(9L, "Actualizada", "Nuevo detalle", newDate,
                Task.Priority.MEDIA, Task.TaskStatus.EN_PROGRESO, 4L, 6L, 8L);
        when(repository.findById(9L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Task result = adapter.save(updated);

        assertEquals("Actualizada", existing.getTitle());
        assertEquals("media", existing.getPriority());
        assertEquals("en_progreso", existing.getStatus());
        assertEquals(4L, existing.getOwnerId());
        assertEquals(6L, existing.getSpaceId());
        assertEquals(Task.TaskStatus.EN_PROGRESO, result.status());
    }

    @Test
    void rejectsUpdatingAMissingTask() {
        Task missing = new Task(90L, "No existe", null, LocalDateTime.now(),
                Task.Priority.BAJA, Task.TaskStatus.PENDIENTE, 1L, null, null);
        when(repository.findById(90L)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> adapter.save(missing));
    }

    @Test
    void readsTasksByOwnerSpaceAndIdentifierAndDeletesThem() {
        TaskJpaEntity first = entity(1L, "Uno", 10L, 20L);
        TaskJpaEntity second = entity(2L, "Dos", 10L, 20L);
        when(repository.findAllByOwnerIdOrderByDueDateAsc(10L)).thenReturn(List.of(first, second));
        when(repository.findAllBySpaceIdOrderByDueDateAsc(20L)).thenReturn(List.of(second));
        when(repository.findById(1L)).thenReturn(Optional.of(first));

        List<Task> byOwner = adapter.findAllByOwnerId(10L);
        List<Task> bySpace = adapter.findAllBySpaceId(20L);
        Optional<Task> found = adapter.findById(1L);

        assertEquals(List.of("Uno", "Dos"), byOwner.stream().map(Task::title).toList());
        assertEquals(2L, bySpace.getFirst().id());
        assertTrue(found.isPresent());
        adapter.delete(found.orElseThrow());
        verify(repository).deleteById(1L);
    }

    private static TaskJpaEntity entity(Long id, String title, Long ownerId, Long spaceId) {
        TaskJpaEntity entity = new TaskJpaEntity(title, "Detalle", LocalDateTime.of(2026, 11, 20, 9, 0),
                "alta", "completada", ownerId, spaceId, null);
        setId(entity, id);
        return entity;
    }

    private static void setId(Object target, Long id) {
        try {
            Field field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
