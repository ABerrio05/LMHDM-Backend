package com.nexo.application.ports.out;

import com.nexo.domain.model.Reminder;
import java.util.List;
import java.util.Optional;

public interface ReminderRepositoryPort {
    Reminder save(Reminder reminder);
    List<Reminder> findAllByTaskId(Long taskId);
    Optional<Reminder> findById(Long reminderId);
    void delete(Reminder reminder);
}
