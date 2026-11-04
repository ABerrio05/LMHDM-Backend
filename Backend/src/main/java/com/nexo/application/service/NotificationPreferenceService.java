package com.nexo.application.service;

import com.nexo.application.ports.in.NotificationPreferenceUseCase;
import com.nexo.application.ports.out.NotificationPreferenceRepositoryPort;
import com.nexo.domain.model.NotificationPreference;
import java.time.LocalDateTime;

public class NotificationPreferenceService implements NotificationPreferenceUseCase {
    private final NotificationPreferenceRepositoryPort preferences;

    public NotificationPreferenceService(NotificationPreferenceRepositoryPort preferences) { this.preferences = preferences; }

    @Override public NotificationPreference getMine(Long userId) {
        return preferences.findByUserId(userId).orElseGet(() -> new NotificationPreference(userId, true, null));
    }

    @Override public NotificationPreference updateMine(Long userId, boolean activityEmailsEnabled) {
        return preferences.save(new NotificationPreference(userId, activityEmailsEnabled, LocalDateTime.now()));
    }

    @Override public boolean activityEmailsEnabledFor(Long userId) { return getMine(userId).activityEmailsEnabled(); }
}
