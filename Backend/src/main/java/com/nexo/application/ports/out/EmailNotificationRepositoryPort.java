package com.nexo.application.ports.out;

import com.nexo.domain.model.EmailNotification;
import java.util.Optional;

public interface EmailNotificationRepositoryPort {
    Optional<EmailNotification> findByReminderId(Long reminderId);
    EmailNotification save(EmailNotification notification);
    void deleteByReminderId(Long reminderId);
    default long countByStatus(String status) { return 0L; }
}
