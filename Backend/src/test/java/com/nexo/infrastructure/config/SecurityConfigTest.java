package com.nexo.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

/** Verifica decisiones de seguridad visibles durante la demostración: hash y CORS. */
class SecurityConfigTest {

    private final SecurityConfig configuration = new SecurityConfig();

    @Test
    void hashesPasswordsWithBcrypt() {
        PasswordEncoder encoder = configuration.passwordEncoder();
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
}
