package com.nexo.application.ports.out;

import java.util.Optional;

public interface TokenProviderPort {
    String createToken(Long userId, String email);
    Optional<AuthenticatedUser> parse(String token);

    record AuthenticatedUser(Long userId, String email) { }
}
