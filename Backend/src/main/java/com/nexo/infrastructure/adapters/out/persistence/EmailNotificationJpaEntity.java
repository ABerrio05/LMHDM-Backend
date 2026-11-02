package com.nexo.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
public class EmailNotificationJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_notificacion") private Long id;
    @Column(name = "id_recordatorio", unique = true) private Long reminderId;
    @Column(name = "id_usuario", nullable = false) private Long userId;
    @Column(name = "id_tarea") private Long taskId;
    @Column(name = "asunto") private String subject;
    @Column(name = "tipo", nullable = false) private String type;
    @Column(name = "estado_envio", nullable = false) private String status;
    @Column(name = "intentos", nullable = false) private int attempts;
    @Column(name = "fecha_envio") private LocalDateTime sentAt;
    @Column(name = "detalle_error") private String errorDetail;

    protected EmailNotificationJpaEntity() { }

    public EmailNotificationJpaEntity(Long reminderId, Long userId, Long taskId, String subject, String status,
                                      int attempts, LocalDateTime sentAt, String errorDetail) {
        this.reminderId = reminderId;
        this.userId = userId;
        this.taskId = taskId;
        this.subject = subject;
        this.type = "correo";
        this.status = status;
        this.attempts = attempts;
        this.sentAt = sentAt;
        this.errorDetail = errorDetail;
    }

    public Long getId() { return id; }
    public Long getReminderId() { return reminderId; }
    public Long getUserId() { return userId; }
    public Long getTaskId() { return taskId; }
    public String getSubject() { return subject; }
    public String getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public LocalDateTime getSentAt() { return sentAt; }
    public String getErrorDetail() { return errorDetail; }

    public void update(String subject, String status, int attempts, LocalDateTime sentAt, String errorDetail) {
        this.subject = subject;
        this.status = status;
        this.attempts = attempts;
        this.sentAt = sentAt;
        this.errorDetail = errorDetail;
    }
}
