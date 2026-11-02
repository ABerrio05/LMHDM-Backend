package com.nexo.infrastructure.adapters.out.persistence;

import com.nexo.application.ports.out.UserRepositoryPort;
import com.nexo.domain.model.User;
import com.nexo.domain.model.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {
    private final SpringDataUserRepository repository;

    public UserPersistenceAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return repository.findById(userId).map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return repository.findAllByOrderByRegisteredAtDesc().stream().map(this::toDomain).toList();
    }

    @Override
    public User updateAdministration(Long userId, UserRole role, boolean active) {
        UserJpaEntity entity = repository.findById(userId).orElseThrow();
        entity.updateAdministration(role, active);
        return toDomain(repository.save(entity));
    }

    @Override
    public User updatePasswordHash(Long userId, String passwordHash) {
        UserJpaEntity entity = repository.findById(userId).orElseThrow();
        entity.updatePasswordHash(passwordHash);
        return toDomain(repository.save(entity));
    }

    @Override
    public User save(User user) {
        UserJpaEntity saved = repository.save(new UserJpaEntity(user.name(), user.email(), user.passwordHash(), user.registeredAt(), user.role()));
        return toDomain(saved);
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(entity.getId(), entity.getName(), entity.getEmail(), entity.getPasswordHash(), entity.getRegisteredAt(), entity.getRole(), entity.isActive());
    }
}
