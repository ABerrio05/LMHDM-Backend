package com.nexo.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "nota")
public class PersonalNoteJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_nota") private Long id;
    @Column(name = "titulo", nullable = false) private String title;
    @Column(name = "contenido", nullable = false) private String content;
    @Column(name = "id_usuario", nullable = false) private Long ownerId;
    @Column(name = "fecha_creacion", nullable = false) private LocalDateTime createdAt;

    protected PersonalNoteJpaEntity() { }
    public PersonalNoteJpaEntity(String title, String content, Long ownerId, LocalDateTime createdAt) {
        this.title = title; this.content = content; this.ownerId = ownerId; this.createdAt = createdAt;
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Long getOwnerId() { return ownerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void update(String title, String content) { this.title = title; this.content = content; }
}
