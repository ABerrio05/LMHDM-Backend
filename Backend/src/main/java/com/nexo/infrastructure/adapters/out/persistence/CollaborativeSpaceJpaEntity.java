package com.nexo.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "espacio_colaborativo")
public class CollaborativeSpaceJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_espacio") private Long id;
    @Column(name = "nombre", nullable = false) private String name;
    @Column(name = "descripcion") private String description;
    @Column(name = "id_usuario_creador", nullable = false) private Long creatorId;
    @Column(name = "fecha_creacion", nullable = false) private LocalDateTime createdAt;

    protected CollaborativeSpaceJpaEntity() { }
    CollaborativeSpaceJpaEntity(String name, String description, Long creatorId, LocalDateTime createdAt) {
        this.name = name; this.description = description; this.creatorId = creatorId; this.createdAt = createdAt;
    }
    Long getId() { return id; }
    String getName() { return name; }
    String getDescription() { return description; }
    Long getCreatorId() { return creatorId; }
    LocalDateTime getCreatedAt() { return createdAt; }
}
