package com.nexo.application.ports.out;

import com.nexo.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    User save(User user);
}
