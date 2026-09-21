package com.nexo.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

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

    protected UserJpaEntity() { }
    public UserJpaEntity(String name, String email, String passwordHash, LocalDateTime registeredAt) {
        this.name = name; this.email = email; this.passwordHash = passwordHash; this.registeredAt = registeredAt;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
}
