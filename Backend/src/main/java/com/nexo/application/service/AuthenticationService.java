package com.nexo.application.service;

import com.nexo.application.ports.in.AuthenticationUseCase;
import com.nexo.application.ports.out.PasswordHasherPort;
import com.nexo.application.ports.out.TokenProviderPort;
import com.nexo.application.ports.out.UserRepositoryPort;
import com.nexo.domain.model.User;
import com.nexo.domain.model.UserRole;
import java.time.LocalDateTime;

/** Caso de uso independiente de HTTP, Spring y PostgreSQL. */
public class AuthenticationService implements AuthenticationUseCase {
    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;
    private final String developerEmail;

    public AuthenticationService(UserRepositoryPort users, PasswordHasherPort passwordHasher, TokenProviderPort tokenProvider) {
        this(users, passwordHasher, tokenProvider, "");
    }

    public AuthenticationService(UserRepositoryPort users, PasswordHasherPort passwordHasher, TokenProviderPort tokenProvider,
                                 String developerEmail) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.developerEmail = developerEmail == null ? "" : developerEmail.trim().toLowerCase();
    }

    @Override
    public AuthenticationResult register(RegisterCommand command) {
        if (command.password() == null || command.password().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        if (users.existsByEmail(command.email().trim().toLowerCase())) {
            throw new EmailAlreadyRegisteredException();
        }
        User saved = users.save(new User(null, command.name(), command.email(), passwordHasher.hash(command.password()), LocalDateTime.now(),
                roleFor(command.email())));
        return resultFor(saved);
    }

    @Override
    public AuthenticationResult login(LoginCommand command) {
        User user = users.findByEmail(command.email().trim().toLowerCase())
                .filter(found -> passwordHasher.matches(command.password(), found.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        if (!user.active()) {
            throw new AccountInactiveException();
        }
        return resultFor(user);
    }

    private AuthenticationResult resultFor(User user) {
        UserRole role = roleFor(user.email()) == UserRole.DEVELOPER ? UserRole.DEVELOPER : user.role();
        return new AuthenticationResult(user.id(), user.name(), user.email(), role,
                tokenProvider.createToken(user.id(), user.email(), role));
    }

    private UserRole roleFor(String email) {
        return !developerEmail.isBlank() && developerEmail.equals(email.trim().toLowerCase())
                ? UserRole.DEVELOPER : UserRole.USER;
    }

    public static class EmailAlreadyRegisteredException extends RuntimeException { }
    public static class InvalidCredentialsException extends RuntimeException { }
    public static class AccountInactiveException extends RuntimeException { }
}
