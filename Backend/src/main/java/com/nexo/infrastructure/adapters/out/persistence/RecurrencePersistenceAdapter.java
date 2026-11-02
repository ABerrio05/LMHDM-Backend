package com.nexo.infrastructure.adapters.out.persistence;

import com.nexo.application.ports.out.RecurrenceRepositoryPort;
import com.nexo.domain.model.Recurrence;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RecurrencePersistenceAdapter implements RecurrenceRepositoryPort {
    private final SpringDataRecurrenceRepository repository;

    public RecurrencePersistenceAdapter(SpringDataRecurrenceRepository repository) { this.repository = repository; }

    @Override public Recurrence save(Recurrence recurrence) {
        return toDomain(repository.save(new RecurrenceJpaEntity(
                recurrence.frequency().name().toLowerCase(Locale.ROOT), recurrence.interval(), recurrence.endDate())));
    }
    @Override public Optional<Recurrence> findById(Long recurrenceId) { return repository.findById(recurrenceId).map(this::toDomain); }
    @Override public void deleteById(Long recurrenceId) { repository.deleteById(recurrenceId); }

    private Recurrence toDomain(RecurrenceJpaEntity entity) {
        return new Recurrence(entity.getId(), Recurrence.Frequency.valueOf(entity.getFrequency().toUpperCase(Locale.ROOT)),
                entity.getIntervalValue(), entity.getEndDate());
    }
}
