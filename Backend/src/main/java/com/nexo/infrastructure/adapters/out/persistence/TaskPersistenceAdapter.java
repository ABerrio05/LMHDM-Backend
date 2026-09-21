package com.nexo.infrastructure.adapters.out.persistence;

import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.domain.model.Task;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TaskPersistenceAdapter implements TaskRepositoryPort {
    private final SpringDataTaskRepository repository;
    public TaskPersistenceAdapter(SpringDataTaskRepository repository) { this.repository = repository; }

    @Override
    public Task save(Task task) {
        TaskJpaEntity entity = new TaskJpaEntity(task.title(), task.description(), task.dueDate(),
                toDatabase(task.priority()), toDatabase(task.status()), task.ownerId(), task.spaceId(), task.recurrenceId());
        if (task.id() == null) return toDomain(repository.save(entity));
        TaskJpaEntity existing = repository.findById(task.id()).orElseThrow();
        existing.update(entity.getTitle(), entity.getDescription(), entity.getDueDate(), entity.getPriority(),
                entity.getStatus(), entity.getSpaceId(), entity.getRecurrenceId());
        return toDomain(repository.save(existing));
    }

    @Override
    public List<Task> findAllByOwnerId(Long ownerId) {
        return repository.findAllByOwnerIdOrderByDueDateAsc(ownerId).stream().map(this::toDomain).toList();
    }
    @Override public Optional<Task> findById(Long taskId) { return repository.findById(taskId).map(this::toDomain); }
    @Override public void delete(Task task) { repository.deleteById(task.id()); }

    private String toDatabase(Enum<?> value) { return value.name().toLowerCase(Locale.ROOT); }
    private Task toDomain(TaskJpaEntity entity) {
        return new Task(entity.getId(), entity.getTitle(), entity.getDescription(), entity.getDueDate(),
                Task.Priority.valueOf(entity.getPriority().toUpperCase(Locale.ROOT)),
                Task.TaskStatus.valueOf(entity.getStatus().toUpperCase(Locale.ROOT)), entity.getOwnerId(),
                entity.getSpaceId(), entity.getRecurrenceId());
    }
}
