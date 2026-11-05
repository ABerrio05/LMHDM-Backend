package com.nexo.application.service;

import com.nexo.application.ports.in.AuthenticationUseCase;
import com.nexo.application.ports.out.PasswordHasherPort;
import com.nexo.application.ports.out.TokenProviderPort;
import com.nexo.application.ports.out.UserRepositoryPort;
import com.nexo.application.ports.out.PendingSpaceInvitationRepositoryPort;
import com.nexo.application.ports.out.SpaceMemberRepositoryPort;
import com.nexo.domain.model.PendingSpaceInvitation;
import com.nexo.domain.model.SpaceMember;
import com.nexo.domain.model.User;
import com.nexo.domain.model.UserRole;
import java.time.LocalDateTime;

/** Caso de uso independiente de HTTP, Spring y PostgreSQL. */
public class AuthenticationService implements AuthenticationUseCase {
    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;
    private final String developerEmail;
    private final PendingSpaceInvitationRepositoryPort pendingInvitations;
    private final SpaceMemberRepositoryPort members;
    private final ActivityEmailNotificationService activityEmails;

    public AuthenticationService(UserRepositoryPort users, PasswordHasherPort passwordHasher, TokenProviderPort tokenProvider) {
        this(users, passwordHasher, tokenProvider, "", null, null, null);
    }

    public AuthenticationService(UserRepositoryPort users, PasswordHasherPort passwordHasher, TokenProviderPort tokenProvider,
                                 String developerEmail) {
        this(users, passwordHasher, tokenProvider, developerEmail, null, null, null);
    }

    public AuthenticationService(UserRepositoryPort users, PasswordHasherPort passwordHasher, TokenProviderPort tokenProvider,
                                 String developerEmail, PendingSpaceInvitationRepositoryPort pendingInvitations,
                                 SpaceMemberRepositoryPort members) {
        this(users, passwordHasher, tokenProvider, developerEmail, pendingInvitations, members, null);
    }

    public AuthenticationService(UserRepositoryPort users, PasswordHasherPort passwordHasher, TokenProviderPort tokenProvider,
                                 String developerEmail, PendingSpaceInvitationRepositoryPort pendingInvitations,
                                 SpaceMemberRepositoryPort members, ActivityEmailNotificationService activityEmails) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.developerEmail = developerEmail == null ? "" : developerEmail.trim().toLowerCase();
        this.pendingInvitations = pendingInvitations;
        this.members = members;
        this.activityEmails = activityEmails;
    }

    @Override
    public AuthenticationResult register(RegisterCommand command) {
        if (command.password() == null || command.password().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        if (users.existsByEmail(command.email().trim().toLowerCase())) {
            throw new EmailAlreadyRegisteredException();
        }
        PendingSpaceInvitation invitation = validateInvitation(command.email(), command.invitationToken());
        User saved = users.save(new User(null, command.name(), command.email(), passwordHasher.hash(command.password()), LocalDateTime.now(),
                roleFor(command.email())));
        acceptInvitation(saved, invitation);
        if (activityEmails != null) activityEmails.accountCreated(saved);
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

    private PendingSpaceInvitation validateInvitation(String email, String token) {
        if (token == null || token.isBlank()) return null;
        if (pendingInvitations == null || members == null) throw new IllegalArgumentException("La invitación no está disponible");
        PendingSpaceInvitation invitation = pendingInvitations.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("La invitación no es válida"));
        if (!invitation.email().equalsIgnoreCase(email) || !invitation.isUsableAt(LocalDateTime.now())) {
            throw new IllegalArgumentException("La invitación venció o corresponde a otro correo");
        }
        return invitation;
    }

    private void acceptInvitation(User user, PendingSpaceInvitation invitation) {
        if (invitation == null) return;
        if (members.findBySpaceIdAndUserId(invitation.spaceId(), user.id()).isEmpty()) {
            members.save(new SpaceMember(null, invitation.spaceId(), user.id(), invitation.role(), LocalDateTime.now()));
        }
        pendingInvitations.save(new PendingSpaceInvitation(invitation.id(), invitation.spaceId(), invitation.email(), invitation.role(),
                invitation.inviterId(), invitation.token(), invitation.expiresAt(), invitation.createdAt(), LocalDateTime.now()));
    }

    public static class EmailAlreadyRegisteredException extends RuntimeException { }
    public static class InvalidCredentialsException extends RuntimeException { }
    public static class AccountInactiveException extends RuntimeException { }
}
