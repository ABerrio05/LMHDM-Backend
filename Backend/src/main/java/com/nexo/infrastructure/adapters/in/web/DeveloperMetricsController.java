package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.service.PlatformNotificationMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/developer/metrics")
@Tag(name = "Administración técnica", description = "Métricas operativas para el usuario desarrollador")
@SecurityRequirement(name = "bearerAuth")
public class DeveloperMetricsController {
    private final PlatformNotificationMetricsService metrics;

    public DeveloperMetricsController(PlatformNotificationMetricsService metrics) { this.metrics = metrics; }

    @GetMapping("/notifications")
    @Operation(summary = "Consultar el estado de entrega de notificaciones por correo")
    public PlatformNotificationMetricsService.NotificationMetrics notifications() { return metrics.summary(); }
}
