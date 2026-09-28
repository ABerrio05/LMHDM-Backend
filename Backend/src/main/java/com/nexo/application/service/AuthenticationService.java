package com.nexo.application.service;

import com.nexo.application.ports.in.AuthenticationUseCase;
import com.nexo.application.ports.out.PasswordHasherPort;
import com.nexo.application.ports.out.TokenProviderPort;
import com.nexo.application.ports.out.UserRepositoryPort;
import com.nexo.domain.model.User;
import java.time.LocalDateTime;

/** Caso de uso independiente de HTTP, Spring y PostgreSQL. */
public class AuthenticationService implements AuthenticationUseCase {
    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;

    public AuthenticationService(UserRepositoryPort users, PasswordHasherPort passwordHasher, TokenProviderPort tokenProvider) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthenticationResult register(RegisterCommand command) {
        if (command.password() == null || command.password().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        if (users.existsByEmail(command.email().trim().toLowerCase())) {
            throw new EmailAlreadyRegisteredException();
        }
        User saved = users.save(new User(null, command.name(), command.email(), passwordHasher.hash(command.password()), LocalDateTime.now()));
        return resultFor(saved);
    }

    @Override
    public AuthenticationResult login(LoginCommand command) {
        User user = users.findByEmail(command.email().trim().toLowerCase())
                .filter(found -> passwordHasher.matches(command.password(), found.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        return resultFor(user);
    }

    private AuthenticationResult resultFor(User user) {
        return new AuthenticationResult(user.id(), user.name(), user.email(), tokenProvider.createToken(user.id(), user.email()));
    }

    public static class EmailAlreadyRegisteredException extends RuntimeException { }
    public static class InvalidCredentialsException extends RuntimeException { }
}
