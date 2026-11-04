package com.nexo.application.ports.in;

import com.nexo.domain.model.NotificationPreference;

public interface NotificationPreferenceUseCase {
    NotificationPreference getMine(Long userId);
    NotificationPreference updateMine(Long userId, boolean activityEmailsEnabled);
    boolean activityEmailsEnabledFor(Long userId);
}
