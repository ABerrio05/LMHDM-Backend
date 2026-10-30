package com.nexo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nexo.application.ports.in.AuthenticationUseCase.LoginCommand;
import com.nexo.application.ports.in.AuthenticationUseCase.RegisterCommand;
import com.nexo.application.ports.out.PasswordHasherPort;
import com.nexo.application.ports.out.TokenProviderPort;
import com.nexo.application.ports.out.UserRepositoryPort;
import com.nexo.domain.model.User;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthenticationServiceTest {
    private InMemoryUserRepository users;
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        PasswordHasherPort hasher = new FakePasswordHasher();
        TokenProviderPort tokens = new FakeTokenProvider();
        service = new AuthenticationService(users, hasher, tokens);
    }

    @Test
    void registersUserWithNormalizedEmailAndReturnsToken() {
        var result = service.register(new RegisterCommand("Andrea", "  ANDREA@NEXO.TEST  ", "segura123"));

        assertEquals("andrea@nexo.test", result.email());
        assertEquals("token-1", result.accessToken());
        assertEquals("hash:segura123", users.findByEmail("andrea@nexo.test").orElseThrow().passwordHash());
    }

    @Test
    void rejectsShortPasswordBeforePersistingUser() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register(new RegisterCommand("Andrea", "andrea@nexo.test", "corta")));

        assertEquals(0, users.size());
    }

    @Test
    void rejectsDuplicateEmailIgnoringCaseAndSpaces() {
        service.register(new RegisterCommand("Andrea", "andrea@nexo.test", "segura123"));

        assertThrows(AuthenticationService.EmailAlreadyRegisteredException.class,
                () -> service.register(new RegisterCommand("Otra", " ANDREA@NEXO.TEST ", "segura123")));
    }

    @Test
    void logsInWithValidCredentials() {
        service.register(new RegisterCommand("Andrea", "andrea@nexo.test", "segura123"));

        var result = service.login(new LoginCommand(" ANDREA@NEXO.TEST ", "segura123"));

        assertEquals("Andrea", result.name());
        assertNotNull(result.accessToken());
    }

    @Test
    void rejectsUnknownUserOrIncorrectPasswordWithSameException() {
        service.register(new RegisterCommand("Andrea", "andrea@nexo.test", "segura123"));

        assertThrows(AuthenticationService.InvalidCredentialsException.class,
                () -> service.login(new LoginCommand("andrea@nexo.test", "incorrecta")));
        assertThrows(AuthenticationService.InvalidCredentialsException.class,
                () -> service.login(new LoginCommand("inexistente@nexo.test", "segura123")));
    }

    private static final class InMemoryUserRepository implements UserRepositoryPort {
        private final Map<String, User> users = new HashMap<>();
        private long nextId = 1;

        @Override
        public boolean existsByEmail(String email) {
            return users.containsKey(email);
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(users.get(email));
        }

        @Override
        public User save(User user) {
            User saved = new User(user.id() == null ? nextId++ : user.id(), user.name(), user.email(),
                    user.passwordHash(), user.registeredAt() == null ? LocalDateTime.now() : user.registeredAt());
            users.put(saved.email(), saved);
            return saved;
        }

        int size() {
            return users.size();
        }
    }

    private static final class FakePasswordHasher implements PasswordHasherPort {
        @Override
        public String hash(String rawPassword) {
            return "hash:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return ("hash:" + rawPassword).equals(passwordHash);
        }
    }

    private static final class FakeTokenProvider implements TokenProviderPort {
        @Override
        public String createToken(Long userId, String email) {
            return "token-" + userId;
        }

        @Override
        public Optional<AuthenticatedUser> parse(String token) {
            return Optional.empty();
        }
    }
}
