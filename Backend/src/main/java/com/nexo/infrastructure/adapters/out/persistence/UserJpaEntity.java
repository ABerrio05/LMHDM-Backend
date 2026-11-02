package com.nexo.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import com.nexo.domain.model.UserRole;

@Entity
@Table(name = "usuario")
public class UserJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;
    @Column(name = "nombre", nullable = false)
    private String name;
    @Column(name = "correo", nullable = false, unique = true)
    private String email;
    @Column(name = "contrasena_hash", nullable = false)
    private String passwordHash;
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime registeredAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "rol_global", nullable = false)
    private UserRole role;
    @Column(name = "activo", nullable = false)
    private boolean active = true;

    protected UserJpaEntity() { }
    public UserJpaEntity(String name, String email, String passwordHash, LocalDateTime registeredAt) {
        this(name, email, passwordHash, registeredAt, UserRole.USER);
    }
    public UserJpaEntity(String name, String email, String passwordHash, LocalDateTime registeredAt, UserRole role) {
        this.name = name; this.email = email; this.passwordHash = passwordHash; this.registeredAt = registeredAt; this.role = role;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return active; }
    public void updateAdministration(UserRole role, boolean active) { this.role = role; this.active = active; }
    public void updatePasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
