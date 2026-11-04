package com.nexo.infrastructure.adapters.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invitacion_espacio")
public class PendingSpaceInvitationJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_invitacion") private Long id;
    @Column(name = "id_espacio", nullable = false) private Long spaceId;
    @Column(name = "correo", nullable = false) private String email;
    @Column(name = "rol", nullable = false) private String role;
    @Column(name = "id_usuario_invitador", nullable = false) private Long inviterId;
    @Column(nullable = false, unique = true) private String token;
    @Column(name = "fecha_expiracion", nullable = false) private LocalDateTime expiresAt;
    @Column(name = "fecha_creacion", nullable = false) private LocalDateTime createdAt;
    @Column(name = "fecha_aceptacion") private LocalDateTime acceptedAt;
    protected PendingSpaceInvitationJpaEntity() { }
    PendingSpaceInvitationJpaEntity(Long spaceId, String email, String role, Long inviterId, String token,
                                    LocalDateTime expiresAt, LocalDateTime createdAt, LocalDateTime acceptedAt) {
        this.spaceId = spaceId; this.email = email; this.role = role; this.inviterId = inviterId; this.token = token;
        this.expiresAt = expiresAt; this.createdAt = createdAt; this.acceptedAt = acceptedAt;
    }
    Long getId() { return id; } Long getSpaceId() { return spaceId; } String getEmail() { return email; }
    String getRole() { return role; } Long getInviterId() { return inviterId; } String getToken() { return token; }
    LocalDateTime getExpiresAt() { return expiresAt; } LocalDateTime getCreatedAt() { return createdAt; } LocalDateTime getAcceptedAt() { return acceptedAt; }
    void accept(LocalDateTime at) { acceptedAt = at; }
}
