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
    @Column(name = "id_recurrencia") private Long recurrenceId;
    protected ReminderJpaEntity() { }
    public ReminderJpaEntity(LocalDateTime scheduledAt, String message, Long taskId, Long recurrenceId) {
        this.scheduledAt = scheduledAt; this.message = message; this.taskId = taskId; this.recurrenceId = recurrenceId;
    }
    public Long getId() { return id; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public String getMessage() { return message; }
    public Long getTaskId() { return taskId; }
    public Long getRecurrenceId() { return recurrenceId; }
    public void update(LocalDateTime scheduledAt, String message, Long recurrenceId) {
        this.scheduledAt = scheduledAt;
        this.message = message;
        this.recurrenceId = recurrenceId;
    }
}
