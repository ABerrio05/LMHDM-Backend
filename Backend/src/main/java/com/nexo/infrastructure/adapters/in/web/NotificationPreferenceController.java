package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.ports.in.NotificationPreferenceUseCase;
import com.nexo.domain.model.NotificationPreference;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification-preferences")
@SecurityRequirement(name = "bearerAuth")
public class NotificationPreferenceController {
    private final NotificationPreferenceUseCase preferences;
    public NotificationPreferenceController(NotificationPreferenceUseCase preferences) { this.preferences = preferences; }
    @GetMapping public NotificationPreference get(@AuthenticationPrincipal Long userId) { return preferences.getMine(userId); }
    @PutMapping public NotificationPreference update(@AuthenticationPrincipal Long userId, @Valid @RequestBody UpdateRequest request) {
        return preferences.updateMine(userId, request.activityEmailsEnabled());
    }
    public record UpdateRequest(@NotNull Boolean activityEmailsEnabled) { }
}
