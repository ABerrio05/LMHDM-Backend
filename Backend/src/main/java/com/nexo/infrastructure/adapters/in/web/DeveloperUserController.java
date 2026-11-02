package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.service.PlatformUserManagementService;
import com.nexo.domain.model.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/developer/users")
@Tag(name = "Desarrollo", description = "Administración técnica de cuentas de NEXO")
public class DeveloperUserController {
    private final PlatformUserManagementService users;

    public DeveloperUserController(PlatformUserManagementService users) { this.users = users; }

    @GetMapping
    @Operation(summary = "Listar cuentas de plataforma")
    public List<PlatformUserManagementService.PlatformUser> list() { return users.list(); }

    @PatchMapping("/{userId}")
    @Operation(summary = "Actualizar rol o estado de una cuenta")
    public PlatformUserManagementService.PlatformUser update(@AuthenticationPrincipal Long currentUserId,
                                                               @PathVariable Long userId,
                                                               @Valid @RequestBody UpdateUserRequest request) {
        return users.update(currentUserId, userId, request.role(), request.active());
    }

    @PatchMapping("/{userId}/password")
    @Operation(summary = "Restablecer una contraseña temporal de una cuenta")
    public PlatformUserManagementService.PlatformUser resetPassword(@AuthenticationPrincipal Long currentUserId,
                                                                      @PathVariable Long userId,
                                                                      @Valid @RequestBody ResetPasswordRequest request) {
        return users.resetPassword(currentUserId, userId, request.temporaryPassword());
    }

    public record UpdateUserRequest(@NotNull UserRole role, boolean active) { }
    public record ResetPasswordRequest(@NotBlank @Size(min = 8, max = 72) String temporaryPassword) { }
}
