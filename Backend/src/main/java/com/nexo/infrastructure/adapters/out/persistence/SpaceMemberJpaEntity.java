package com.nexo.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "miembro_espacio")
public class SpaceMemberJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_miembro") private Long id;
    @Column(name = "id_espacio", nullable = false) private Long spaceId;
    @Column(name = "id_usuario", nullable = false) private Long userId;
    @Column(name = "rol", nullable = false) private String role;
    @Column(name = "fecha_ingreso", nullable = false) private LocalDateTime joinedAt;

    protected SpaceMemberJpaEntity() { }
    SpaceMemberJpaEntity(Long spaceId, Long userId, String role, LocalDateTime joinedAt) {
        this.spaceId = spaceId; this.userId = userId; this.role = role; this.joinedAt = joinedAt;
    }
    Long getId() { return id; }
    Long getSpaceId() { return spaceId; }
    Long getUserId() { return userId; }
    String getRole() { return role; }
    LocalDateTime getJoinedAt() { return joinedAt; }
}
