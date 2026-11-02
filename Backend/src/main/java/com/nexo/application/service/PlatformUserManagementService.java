package com.nexo.application.service;

import com.nexo.application.ports.out.UserRepositoryPort;
import com.nexo.application.ports.out.PasswordHasherPort;
import com.nexo.domain.model.User;
import com.nexo.domain.model.UserRole;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/** Administración de cuentas de la plataforma, disponible únicamente al desarrollador. */
@Service
public class PlatformUserManagementService {
    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;

    public PlatformUserManagementService(UserRepositoryPort users, PasswordHasherPort passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    public List<PlatformUser> list() {
        return users.findAll().stream().map(this::safeView).toList();
    }

    public PlatformUser update(Long requesterId, Long userId, UserRole role, boolean active) {
        User target = users.findById(userId).orElseThrow(PlatformUserNotFoundException::new);
        if (requesterId.equals(userId) && (!active || role != UserRole.DEVELOPER)) {
            throw new IllegalArgumentException("No puedes retirar tu propio acceso de desarrollador");
        }
        if (role == UserRole.DEVELOPER && target.role() != UserRole.DEVELOPER) {
            throw new IllegalArgumentException("El rol de desarrollador se configura mediante APP_DEVELOPER_EMAIL");
        }
        return safeView(users.updateAdministration(userId, role, active));
    }

    /** El desarrollador define una clave temporal sin recuperar ni exponer la anterior. */
    public PlatformUser resetPassword(Long requesterId, Long userId, String temporaryPassword) {
        if (temporaryPassword == null || temporaryPassword.length() < 8 || temporaryPassword.length() > 72) {
            throw new IllegalArgumentException("La contraseña temporal debe tener entre 8 y 72 caracteres");
        }
        User target = users.findById(userId).orElseThrow(PlatformUserNotFoundException::new);
        if (requesterId.equals(userId)) {
            throw new IllegalArgumentException("Usa el flujo de inicio de sesión para cambiar tu propia contraseña");
        }
        return safeView(users.updatePasswordHash(target.id(), passwordHasher.hash(temporaryPassword)));
    }

    private PlatformUser safeView(User user) {
        return new PlatformUser(user.id(), user.name(), user.email(), user.role(), user.active(), user.registeredAt());
    }

    public record PlatformUser(Long id, String name, String email, UserRole role, boolean active,
                               LocalDateTime registeredAt) { }
    public static class PlatformUserNotFoundException extends RuntimeException { }
}
