package com.nexo.application.ports.out;

import com.nexo.domain.model.User;
import com.nexo.domain.model.UserRole;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long userId);
    default List<User> findAll() { return List.of(); }
    default User updateAdministration(Long userId, UserRole role, boolean active) {
        throw new UnsupportedOperationException("La administración de usuarios no está disponible");
    }
    default User updatePasswordHash(Long userId, String passwordHash) {
        throw new UnsupportedOperationException("La recuperación de acceso no está disponible");
    }
    User save(User user);
}
