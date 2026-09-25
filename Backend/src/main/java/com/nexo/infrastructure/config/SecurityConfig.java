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
import com.nexo.application.service.TaskManagementService;
import com.nexo.application.ports.in.ReminderManagementUseCase;
import com.nexo.application.ports.out.ReminderRepositoryPort;
import com.nexo.application.service.ReminderManagementService;
import com.nexo.infrastructure.adapters.in.web.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/health", "/api/auth/**").permitAll().anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    AuthenticationUseCase authenticationUseCase(UserRepositoryPort users, PasswordHasherPort passwordHasher,
                                                TokenProviderPort tokenProvider) {
        return new AuthenticationService(users, passwordHasher, tokenProvider);
    }

    @Bean
    TaskManagementUseCase taskManagementUseCase(TaskRepositoryPort tasks) {
        return new TaskManagementService(tasks);
    }

    @Bean
    ReminderManagementUseCase reminderManagementUseCase(ReminderRepositoryPort reminders, TaskRepositoryPort tasks) {
        return new ReminderManagementService(reminders, tasks);
    }
}
