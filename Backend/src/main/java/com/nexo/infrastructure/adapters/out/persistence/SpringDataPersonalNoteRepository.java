package com.nexo.infrastructure.adapters.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPersonalNoteRepository extends JpaRepository<PersonalNoteJpaEntity, Long> {
    List<PersonalNoteJpaEntity> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
