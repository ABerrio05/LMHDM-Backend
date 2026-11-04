package com.nexo.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "preferencia_notificacion")
public class NotificationPreferenceJpaEntity {
    @Id @Column(name = "id_usuario") private Long userId;
    @Column(name = "correos_actividad_habilitados", nullable = false) private boolean activityEmailsEnabled;
    @Column(name = "fecha_actualizacion", nullable = false) private LocalDateTime updatedAt;
    protected NotificationPreferenceJpaEntity() { }
    NotificationPreferenceJpaEntity(Long userId, boolean activityEmailsEnabled, LocalDateTime updatedAt) {
        this.userId = userId; this.activityEmailsEnabled = activityEmailsEnabled; this.updatedAt = updatedAt;
    }
    Long getUserId() { return userId; } boolean isActivityEmailsEnabled() { return activityEmailsEnabled; }
    LocalDateTime getUpdatedAt() { return updatedAt; }
    void update(boolean enabled, LocalDateTime at) { activityEmailsEnabled = enabled; updatedAt = at; }
}
