package com.nexo.infrastructure.adapters.out.persistence;

import com.nexo.application.ports.out.NotificationPreferenceRepositoryPort;
import com.nexo.domain.model.NotificationPreference;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class NotificationPreferencePersistenceAdapter implements NotificationPreferenceRepositoryPort {
    private final SpringDataNotificationPreferenceRepository repository;
    public NotificationPreferencePersistenceAdapter(SpringDataNotificationPreferenceRepository repository) { this.repository = repository; }
    @Override public Optional<NotificationPreference> findByUserId(Long userId) { return repository.findById(userId).map(this::toDomain); }
    @Override public NotificationPreference save(NotificationPreference preference) {
        NotificationPreferenceJpaEntity entity = repository.findById(preference.userId())
                .orElseGet(() -> new NotificationPreferenceJpaEntity(preference.userId(), preference.activityEmailsEnabled(), preference.updatedAt()));
        entity.update(preference.activityEmailsEnabled(), preference.updatedAt());
        return toDomain(repository.save(entity));
    }
    private NotificationPreference toDomain(NotificationPreferenceJpaEntity entity) {
        return new NotificationPreference(entity.getUserId(), entity.isActivityEmailsEnabled(), entity.getUpdatedAt());
    }
}
