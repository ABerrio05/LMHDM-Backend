package com.nexo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nexo.application.ports.out.PasswordHasherPort;
import com.nexo.application.ports.out.UserRepositoryPort;
import com.nexo.domain.model.User;
import com.nexo.domain.model.UserRole;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlatformUserManagementServiceTest {
    private InMemoryUsers users;
    private PlatformUserManagementService service;

    @BeforeEach
    void setUp() {
        users = new InMemoryUsers();
        users.save(new User(1L, "Desarrollador", "dev@nexo.test", "hash:actual", LocalDateTime.now(), UserRole.DEVELOPER, true));
        users.save(new User(2L, "Ana", "ana@nexo.test", "hash:anterior", LocalDateTime.now(), UserRole.USER, true));
        service = new PlatformUserManagementService(users, new FakeHasher());
    }

    @Test
    void resetsAnotherUsersPasswordWithoutExposingIt() {
        var result = service.resetPassword(1L, 2L, "Temporal123");

        assertEquals("ana@nexo.test", result.email());
        assertEquals("hash:Temporal123", users.findById(2L).orElseThrow().passwordHash());
    }

    @Test
    void doesNotAllowDeveloperToResetOwnPassword() {
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(1L, 1L, "Temporal123"));
    }

    @Test
    void requiresAStrongTemporaryPassword() {
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(1L, 2L, "corta"));
        assertEquals("hash:anterior", users.findById(2L).orElseThrow().passwordHash());
    }

    private static final class FakeHasher implements PasswordHasherPort {
        @Override public String hash(String rawPassword) { return "hash:" + rawPassword; }
        @Override public boolean matches(String rawPassword, String passwordHash) { return hash(rawPassword).equals(passwordHash); }
    }

    private static final class InMemoryUsers implements UserRepositoryPort {
        private final List<User> data = new ArrayList<>();

        @Override public boolean existsByEmail(String email) { return findByEmail(email).isPresent(); }
        @Override public Optional<User> findByEmail(String email) { return data.stream().filter(user -> user.email().equals(email)).findFirst(); }
        @Override public Optional<User> findById(Long id) { return data.stream().filter(user -> user.id().equals(id)).findFirst(); }
        @Override public List<User> findAll() { return List.copyOf(data); }
        @Override public User save(User user) { data.removeIf(current -> current.id().equals(user.id())); data.add(user); return user; }
        @Override public User updatePasswordHash(Long id, String hash) {
            User current = findById(id).orElseThrow();
            User updated = new User(current.id(), current.name(), current.email(), hash, current.registeredAt(), current.role(), current.active());
            return save(updated);
        }
    }
}
