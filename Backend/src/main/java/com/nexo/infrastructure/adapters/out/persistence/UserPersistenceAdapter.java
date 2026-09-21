package com.nexo.infrastructure.adapters.out.persistence;

import com.nexo.application.ports.out.UserRepositoryPort;
import com.nexo.domain.model.User;
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
    public User save(User user) {
        UserJpaEntity saved = repository.save(new UserJpaEntity(user.name(), user.email(), user.passwordHash(), user.registeredAt()));
        return toDomain(saved);
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(entity.getId(), entity.getName(), entity.getEmail(), entity.getPasswordHash(), entity.getRegisteredAt());
    }
}
