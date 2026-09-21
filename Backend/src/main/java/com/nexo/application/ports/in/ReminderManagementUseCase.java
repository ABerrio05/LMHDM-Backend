package com.nexo.application.ports.in;

import com.nexo.domain.model.Reminder;
import java.time.LocalDateTime;
import java.util.List;

public interface ReminderManagementUseCase {
    Reminder create(Long ownerId, Long taskId, CreateReminderCommand command);
    List<Reminder> listForTask(Long ownerId, Long taskId);
    void delete(Long ownerId, Long reminderId);

    record CreateReminderCommand(LocalDateTime scheduledAt, String message) { }
}
