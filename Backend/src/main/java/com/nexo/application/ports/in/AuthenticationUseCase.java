package com.nexo.application.ports.in;

import com.nexo.domain.model.UserRole;

public interface AuthenticationUseCase {
    AuthenticationResult register(RegisterCommand command);
    AuthenticationResult login(LoginCommand command);

    record RegisterCommand(String name, String email, String password) { }
    record LoginCommand(String email, String password) { }
    record AuthenticationResult(Long userId, String name, String email, UserRole role, String accessToken) { }
}
