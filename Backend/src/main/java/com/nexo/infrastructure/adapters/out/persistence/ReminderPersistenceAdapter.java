package com.nexo.infrastructure.adapters.out.persistence;

import com.nexo.application.ports.out.ReminderRepositoryPort;
import com.nexo.domain.model.Reminder;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ReminderPersistenceAdapter implements ReminderRepositoryPort {
    private final SpringDataReminderRepository repository;
    public ReminderPersistenceAdapter(SpringDataReminderRepository repository) { this.repository = repository; }
    @Override public Reminder save(Reminder reminder) {
        ReminderJpaEntity saved = repository.save(new ReminderJpaEntity(reminder.scheduledAt(), reminder.message(), reminder.taskId()));
        return toDomain(saved);
    }
    @Override public List<Reminder> findAllByTaskId(Long taskId) {
        return repository.findAllByTaskIdOrderByScheduledAtAsc(taskId).stream().map(this::toDomain).toList();
    }
    @Override public Optional<Reminder> findById(Long reminderId) { return repository.findById(reminderId).map(this::toDomain); }
    @Override public void delete(Reminder reminder) { repository.deleteById(reminder.id()); }
    private Reminder toDomain(ReminderJpaEntity entity) { return new Reminder(entity.getId(), entity.getScheduledAt(), entity.getMessage(), entity.getTaskId()); }
}
