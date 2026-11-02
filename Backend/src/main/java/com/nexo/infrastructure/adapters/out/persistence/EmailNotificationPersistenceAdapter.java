package com.nexo.infrastructure.adapters.out.persistence;

import com.nexo.application.ports.out.EmailNotificationRepositoryPort;
import com.nexo.domain.model.EmailNotification;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationPersistenceAdapter implements EmailNotificationRepositoryPort {
    private final SpringDataEmailNotificationRepository repository;

    public EmailNotificationPersistenceAdapter(SpringDataEmailNotificationRepository repository) {
        this.repository = repository;
    }

    @Override public Optional<EmailNotification> findByReminderId(Long reminderId) {
        return repository.findByReminderId(reminderId).map(this::toDomain);
    }

    @Override public EmailNotification save(EmailNotification notification) {
        EmailNotificationJpaEntity entity = repository.findByReminderId(notification.reminderId())
                .orElseGet(() -> new EmailNotificationJpaEntity(notification.reminderId(), notification.userId(),
                        notification.taskId(), notification.subject(), notification.status(), notification.attempts(),
                        notification.sentAt(), notification.errorDetail()));
        if (entity.getId() != null) {
            entity.update(notification.subject(), notification.status(), notification.attempts(), notification.sentAt(),
                    notification.errorDetail());
        }
        return toDomain(repository.save(entity));
    }

    @Override public void deleteByReminderId(Long reminderId) { repository.deleteByReminderId(reminderId); }

    @Override public long countByStatus(String status) { return repository.countByStatus(status); }

    private EmailNotification toDomain(EmailNotificationJpaEntity entity) {
        return new EmailNotification(entity.getId(), entity.getReminderId(), entity.getUserId(), entity.getTaskId(),
                entity.getSubject(), entity.getStatus(), entity.getAttempts(), entity.getSentAt(), entity.getErrorDetail());
    }
}
