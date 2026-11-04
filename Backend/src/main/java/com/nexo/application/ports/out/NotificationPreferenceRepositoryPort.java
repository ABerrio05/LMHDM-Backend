package com.nexo.application.ports.out;

import com.nexo.domain.model.NotificationPreference;
import java.util.Optional;

public interface NotificationPreferenceRepositoryPort {
    Optional<NotificationPreference> findByUserId(Long userId);
    NotificationPreference save(NotificationPreference preference);
}
