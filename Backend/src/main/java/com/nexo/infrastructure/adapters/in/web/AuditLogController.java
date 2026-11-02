package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.ports.in.AuditLogUseCase;
import com.nexo.domain.model.AuditLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/developer/audit-logs")
@Validated
@Tag(name = "Administración técnica", description = "Trazabilidad de la plataforma para el usuario desarrollador")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {
    private final AuditLogUseCase auditLogs;
    public AuditLogController(AuditLogUseCase auditLogs) { this.auditLogs = auditLogs; }

    @GetMapping
    @Operation(summary = "Consultar los registros recientes de la plataforma")
    public List<AuditLog> listPlatform(@RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
        return auditLogs.listPlatform(limit);
    }
}
