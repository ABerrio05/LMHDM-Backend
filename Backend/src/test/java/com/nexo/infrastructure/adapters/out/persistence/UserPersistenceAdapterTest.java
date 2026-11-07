package com.nexo.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexo.domain.model.User;
import com.nexo.domain.model.UserRole;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UserPersistenceAdapterTest {

    @Mock
    private SpringDataUserRepository repository;

    private UserPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new UserPersistenceAdapter(repository);
    }

    @Test
    void savesAndMapsAUser() {
        LocalDateTime registeredAt = LocalDateTime.of(2026, 11, 7, 8, 30);
        User user = new User(null, "Nexo Team", "nexo@example.com", "hash", registeredAt,
                UserRole.USER, true);
        when(repository.save(any(UserJpaEntity.class))).thenAnswer(invocation -> {
            UserJpaEntity saved = invocation.getArgument(0);
            setId(saved, 22L);
            return saved;
        });

        User result = adapter.save(user);

        assertEquals(22L, result.id());
        assertEquals("Nexo Team", result.name());
        assertEquals("nexo@example.com", result.email());
        assertEquals(UserRole.USER, result.role());
        assertTrue(result.active());
    }

    @Test
    void findsUsersAndChecksExistingEmail() {
        UserJpaEntity first = entity(1L, "Ana", "ana@example.com");
        UserJpaEntity second = entity(2L, "Luis", "luis@example.com");
        when(repository.existsByEmail("ana@example.com")).thenReturn(true);
        when(repository.findByEmail("ana@example.com")).thenReturn(Optional.of(first));
        when(repository.findById(2L)).thenReturn(Optional.of(second));
        when(repository.findAllByOrderByRegisteredAtDesc()).thenReturn(List.of(second, first));

        assertTrue(adapter.existsByEmail("ana@example.com"));
        assertEquals("Ana", adapter.findByEmail("ana@example.com").orElseThrow().name());
        assertEquals("Luis", adapter.findById(2L).orElseThrow().name());
        assertEquals(List.of("Luis", "Ana"), adapter.findAll().stream().map(User::name).toList());
    }

    @Test
    void updatesAdministrationAndPassword() {
        UserJpaEntity entity = entity(4L, "Carlos", "carlos@example.com");
        when(repository.findById(4L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        User inactive = adapter.updateAdministration(4L, UserRole.USER, false);
        User withNewPassword = adapter.updatePasswordHash(4L, "new-hash");

        assertFalse(inactive.active());
        assertEquals("new-hash", withNewPassword.passwordHash());
        verify(repository, org.mockito.Mockito.times(2)).save(entity);
    }

    @Test
    void rejectsUpdatesForUnknownUsers() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class,
                () -> adapter.updateAdministration(99L, UserRole.USER, false));
        assertThrows(java.util.NoSuchElementException.class,
                () -> adapter.updatePasswordHash(99L, "hash"));
    }

    private static UserJpaEntity entity(Long id, String name, String email) {
        UserJpaEntity entity = new UserJpaEntity(name, email, "hash",
                LocalDateTime.of(2026, 11, 7, 7, 0), UserRole.USER);
        setId(entity, id);
        return entity;
    }

    private static void setId(Object target, Long id) {
        try {
            Field field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
