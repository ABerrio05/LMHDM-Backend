package com.nexo.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_log")
public class AuditLogJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_log") private Long id;
    @Column(name = "id_usuario", nullable = false) private Long userId;
    @Column(name = "accion", nullable = false, length = 150) private String action;
    @Column(name = "metodo", nullable = false, length = 10) private String method;
    @Column(name = "ruta", nullable = false, length = 500) private String path;
    @Column(name = "codigo_estado", nullable = false) private int statusCode;
    @Column(name = "duracion_ms", nullable = false) private long durationMs;
    @Column(name = "request_id", nullable = false, length = 64) private String requestId;
    @Column(name = "fecha_creacion", nullable = false) private LocalDateTime createdAt;

    protected AuditLogJpaEntity() { }
    public AuditLogJpaEntity(Long userId, String action, String method, String path, int statusCode,
                             long durationMs, String requestId, LocalDateTime createdAt) {
        this.userId = userId; this.action = action; this.method = method; this.path = path;
        this.statusCode = statusCode; this.durationMs = durationMs; this.requestId = requestId; this.createdAt = createdAt;
    }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getAction() { return action; }
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public int getStatusCode() { return statusCode; }
    public long getDurationMs() { return durationMs; }
    public String getRequestId() { return requestId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
