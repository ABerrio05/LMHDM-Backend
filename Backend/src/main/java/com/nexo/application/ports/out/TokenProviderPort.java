package com.nexo.application.ports.out;

import java.util.Optional;
import com.nexo.domain.model.UserRole;

public interface TokenProviderPort {
    String createToken(Long userId, String email, UserRole role);
    Optional<AuthenticatedUser> parse(String token);

    record AuthenticatedUser(Long userId, String email, UserRole role) { }
}
