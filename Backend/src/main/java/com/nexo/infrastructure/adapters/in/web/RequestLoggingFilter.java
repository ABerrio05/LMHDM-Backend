package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.ports.in.AuditLogUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Registra cada petición sin escribir contraseñas, tokens ni cuerpos de solicitud. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private final AuditLogUseCase auditLogs;

    public RequestLoggingFilter(AuditLogUseCase auditLogs) { this.auditLogs = auditLogs; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = Optional.ofNullable(request.getHeader(REQUEST_ID_HEADER))
                .filter(value -> !value.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());
        long startedAt = System.nanoTime();
        MDC.put("requestId", requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.info("http_request requestId={} method={} path={} status={} durationMs={}", requestId,
                    request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
            persistAuditLog(request, response, requestId, durationMs);
            MDC.remove("requestId");
        }
    }

    private void persistAuditLog(HttpServletRequest request, HttpServletResponse response, String requestId, long durationMs) {
        Object value = request.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USER_ID);
        if (!(value instanceof Long userId) || !isAuditableApiRequest(request.getRequestURI())) return;
        try {
            String path = request.getRequestURI();
            auditLogs.record(userId, request.getMethod() + " " + path, request.getMethod(), path,
                    response.getStatus(), durationMs, requestId);
        } catch (RuntimeException exception) {
            // La auditoría nunca debe impedir que la operación principal responda al usuario.
            log.warn("audit_log_not_persisted requestId={} reason={}", requestId, exception.getClass().getSimpleName());
        }
    }

    private boolean isAuditableApiRequest(String path) {
        return path.startsWith("/api/") && !path.equals("/api/health") && !path.startsWith("/api/audit-logs");
    }
}
