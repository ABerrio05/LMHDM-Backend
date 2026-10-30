package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.ports.in.AuditLogUseCase;
import com.nexo.domain.model.AuditLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Actividad", description = "Historial de acciones del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {
    private final AuditLogUseCase auditLogs;
    public AuditLogController(AuditLogUseCase auditLogs) { this.auditLogs = auditLogs; }

    @GetMapping
    @Operation(summary = "Consultar mi actividad reciente")
    public List<AuditLog> listMine(@AuthenticationPrincipal Long userId,
                                   @RequestParam(defaultValue = "50") int limit) {
        return auditLogs.listMine(userId, limit);
    }
}
