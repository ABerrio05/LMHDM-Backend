package com.nexo.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import java.util.Arrays;
import java.util.List;
import com.nexo.application.ports.in.AuthenticationUseCase;
import com.nexo.application.ports.out.PasswordHasherPort;
import com.nexo.application.ports.out.TokenProviderPort;
import com.nexo.application.ports.out.UserRepositoryPort;
import com.nexo.application.service.AuthenticationService;
import com.nexo.application.ports.in.TaskManagementUseCase;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.application.ports.out.RecurrenceRepositoryPort;
import com.nexo.application.service.TaskManagementService;
import com.nexo.application.ports.in.ReminderManagementUseCase;
import com.nexo.application.ports.out.ReminderRepositoryPort;
import com.nexo.application.ports.out.EmailNotificationRepositoryPort;
import com.nexo.application.service.ReminderManagementService;
import com.nexo.application.ports.in.CollaborativeSpaceUseCase;
import com.nexo.application.ports.out.CollaborativeSpaceRepositoryPort;
import com.nexo.application.ports.out.SpaceMemberRepositoryPort;
import com.nexo.application.service.CollaborativeSpaceService;
import com.nexo.infrastructure.adapters.in.web.JwtAuthenticationFilter;
import com.nexo.application.ports.in.HolidayCalendarUseCase;
import com.nexo.application.ports.out.HolidayCalendarProviderPort;
import com.nexo.application.service.HolidayCalendarService;
import com.nexo.application.ports.in.NoteManagementUseCase;
import com.nexo.application.ports.out.PersonalNoteRepositoryPort;
import com.nexo.application.service.NoteManagementService;
import com.nexo.application.ports.in.AuditLogUseCase;
import com.nexo.application.ports.out.AuditLogRepositoryPort;
import com.nexo.application.service.AuditLogService;
import com.nexo.application.ports.in.TaskAssistantUseCase;
import com.nexo.application.ports.out.TaskAssistantProviderPort;
import com.nexo.application.service.TaskAssistantService;
import com.nexo.application.ports.out.EmailDeliveryPort;
import com.nexo.application.service.ActivityEmailNotificationService;
import com.nexo.application.ports.out.PendingSpaceInvitationRepositoryPort;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health", "/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        .requestMatchers("/api/developer/**").hasRole("DEVELOPER")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder(@Value("${app.security.bcrypt-strength:10}") int bcryptStrength) {
        return new BCryptPasswordEncoder(bcryptStrength);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "ngrok-skip-browser-warning"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    AuthenticationUseCase authenticationUseCase(UserRepositoryPort users, PasswordHasherPort passwordHasher,
                                                TokenProviderPort tokenProvider,
                                                @Value("${app.developer.email:}") String developerEmail,
                                                PendingSpaceInvitationRepositoryPort pendingInvitations,
                                                SpaceMemberRepositoryPort members) {
        return new AuthenticationService(users, passwordHasher, tokenProvider, developerEmail, pendingInvitations, members);
    }

    @Bean
    TaskManagementUseCase taskManagementUseCase(TaskRepositoryPort tasks, SpaceMemberRepositoryPort members,
                                                RecurrenceRepositoryPort recurrences, ReminderRepositoryPort reminders,
                                                UserRepositoryPort users, ActivityEmailNotificationService activityEmails) {
        return new TaskManagementService(tasks, members, recurrences, reminders, users, activityEmails);
    }

    @Bean
    ReminderManagementUseCase reminderManagementUseCase(ReminderRepositoryPort reminders, TaskRepositoryPort tasks,
                                                         EmailNotificationRepositoryPort notifications,
                                                         RecurrenceRepositoryPort recurrences) {
        return new ReminderManagementService(reminders, tasks, notifications, recurrences);
    }

    @Bean
    CollaborativeSpaceUseCase collaborativeSpaceUseCase(CollaborativeSpaceRepositoryPort spaces,
                                                         SpaceMemberRepositoryPort members, UserRepositoryPort users,
                                                         TaskRepositoryPort tasks, ActivityEmailNotificationService activityEmails,
                                                         PendingSpaceInvitationRepositoryPort pendingInvitations,
                                                         @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl) {
        return new CollaborativeSpaceService(spaces, members, users, tasks, activityEmails, pendingInvitations, frontendUrl);
    }

    @Bean
    HolidayCalendarUseCase holidayCalendarUseCase(HolidayCalendarProviderPort holidayProvider) {
        return new HolidayCalendarService(holidayProvider);
    }

    @Bean
    NoteManagementUseCase noteManagementUseCase(PersonalNoteRepositoryPort notes) {
        return new NoteManagementService(notes);
    }

    @Bean
    AuditLogUseCase auditLogUseCase(AuditLogRepositoryPort auditLogs) {
        return new AuditLogService(auditLogs);
    }

    @Bean
    TaskAssistantUseCase taskAssistantUseCase(TaskRepositoryPort tasks, PersonalNoteRepositoryPort notes,
                                              TaskAssistantProviderPort provider) {
        return new TaskAssistantService(tasks, notes, provider);
    }

    @Bean
    ActivityEmailNotificationService activityEmailNotificationService(EmailDeliveryPort emailDelivery) {
        return new ActivityEmailNotificationService(emailDelivery);
    }
}
