package com.nexo.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarea")
public class TaskJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_tarea") private Long id;
    @Column(name = "titulo", nullable = false) private String title;
    @Column(name = "descripcion") private String description;
    @Column(name = "fecha_vencimiento") private LocalDateTime dueDate;
    @Column(name = "prioridad", nullable = false) private String priority;
    @Column(name = "estado", nullable = false) private String status;
    @Column(name = "id_usuario", nullable = false) private Long ownerId;
    @Column(name = "id_espacio") private Long spaceId;
    @Column(name = "id_recurrencia") private Long recurrenceId;

    protected TaskJpaEntity() { }
    public TaskJpaEntity(String title, String description, LocalDateTime dueDate, String priority, String status,
                         Long ownerId, Long spaceId, Long recurrenceId) {
        this.title = title; this.description = description; this.dueDate = dueDate; this.priority = priority;
        this.status = status; this.ownerId = ownerId; this.spaceId = spaceId; this.recurrenceId = recurrenceId;
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getDueDate() { return dueDate; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public Long getOwnerId() { return ownerId; }
    public Long getSpaceId() { return spaceId; }
    public Long getRecurrenceId() { return recurrenceId; }
    public void update(String title, String description, LocalDateTime dueDate, String priority, String status,
                       Long spaceId, Long recurrenceId) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.spaceId = spaceId;
        this.recurrenceId = recurrenceId;
    }
}
