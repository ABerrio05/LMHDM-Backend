package com.nexo.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nexo.application.ports.in.NotificationPreferenceUseCase;
import com.nexo.application.ports.in.TaskAssistantUseCase;
import com.nexo.application.ports.out.EmailDeliveryPort;
import com.nexo.application.ports.out.PersonalNoteRepositoryPort;
import com.nexo.application.ports.out.TaskAssistantProviderPort;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.application.service.ActivityEmailNotificationService;
import com.nexo.application.service.TaskAssistantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

/** Verifica decisiones de seguridad visibles durante la demostración: hash y CORS. */
class SecurityConfigTest {

    private final SecurityConfig configuration = new SecurityConfig();

    @Test
    void hashesPasswordsWithBcrypt() {
        PasswordEncoder encoder = configuration.passwordEncoder(10);
        String rawPassword = "ClaveSegura123";

        String hash = encoder.encode(rawPassword);

        assertFalse(rawPassword.equals(hash));
        assertTrue(encoder.matches(rawPassword, hash));
    }

    @Test
    void allowsAngularAndNgrokWarningHeaderThroughCors() {
        CorsConfigurationSource source = configuration.corsConfigurationSource("http://localhost:4200");
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/tasks");
        CorsConfiguration cors = source.getCorsConfiguration(request);

        assertTrue(cors.getAllowedOrigins().contains("http://localhost:4200"));
        assertTrue(cors.getAllowedMethods().contains("PATCH"));
        assertTrue(cors.getAllowedHeaders().contains("Authorization"));
        assertTrue(cors.getAllowedHeaders().contains("ngrok-skip-browser-warning"));
    }

    @Test
    void createsAssistantWithTheOnlyConfiguredProvider() {
        @SuppressWarnings("unchecked")
        ObjectProvider<TaskAssistantProviderPort> providers = mock(ObjectProvider.class);
        when(providers.getIfUnique()).thenReturn(mock(TaskAssistantProviderPort.class));

        TaskAssistantUseCase assistant = configuration.taskAssistantUseCase(
                mock(TaskRepositoryPort.class), mock(PersonalNoteRepositoryPort.class), providers);

        assertInstanceOf(TaskAssistantService.class, assistant);
    }

    @Test
    void rejectsAssistantWhenThereIsNoUniqueProvider() {
        @SuppressWarnings("unchecked")
        ObjectProvider<TaskAssistantProviderPort> providers = mock(ObjectProvider.class);
        when(providers.getIfUnique()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> configuration.taskAssistantUseCase(
                mock(TaskRepositoryPort.class), mock(PersonalNoteRepositoryPort.class), providers));
    }

    @Test
    void createsEmailNotificationsWithTheOnlyConfiguredDeliveryService() {
        @SuppressWarnings("unchecked")
        ObjectProvider<EmailDeliveryPort> deliveries = mock(ObjectProvider.class);
        when(deliveries.getIfUnique()).thenReturn(mock(EmailDeliveryPort.class));
        TaskExecutor executor = configuration.notificationEmailExecutor();

        ActivityEmailNotificationService service = configuration.activityEmailNotificationService(
                deliveries, mock(NotificationPreferenceUseCase.class), executor);

        assertInstanceOf(ActivityEmailNotificationService.class, service);
    }

    @Test
    void rejectsEmailNotificationsWhenThereIsNoUniqueDeliveryService() {
        @SuppressWarnings("unchecked")
        ObjectProvider<EmailDeliveryPort> deliveries = mock(ObjectProvider.class);
        when(deliveries.getIfUnique()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> configuration.activityEmailNotificationService(
                deliveries, mock(NotificationPreferenceUseCase.class), configuration.notificationEmailExecutor()));
    }
}
