package com.nexo.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "recordatorio")
public class ReminderJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_recordatorio") private Long id;
    @Column(name = "fecha_hora", nullable = false) private LocalDateTime scheduledAt;
    @Column(name = "mensaje", nullable = false) private String message;
    @Column(name = "id_tarea", nullable = false) private Long taskId;
    protected ReminderJpaEntity() { }
    public ReminderJpaEntity(LocalDateTime scheduledAt, String message, Long taskId) {
        this.scheduledAt = scheduledAt; this.message = message; this.taskId = taskId;
    }
    public Long getId() { return id; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public String getMessage() { return message; }
    public Long getTaskId() { return taskId; }
}
