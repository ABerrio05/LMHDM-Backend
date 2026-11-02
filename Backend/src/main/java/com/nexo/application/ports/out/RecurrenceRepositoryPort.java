package com.nexo.application.ports.out;

import com.nexo.domain.model.Recurrence;
import java.util.Optional;

public interface RecurrenceRepositoryPort {
    Recurrence save(Recurrence recurrence);
    Optional<Recurrence> findById(Long recurrenceId);
    void deleteById(Long recurrenceId);
}
