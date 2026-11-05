package com.nexo.infrastructure.adapters.out.persistence;

import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

interface SpringDataPendingSpaceInvitationRepository extends JpaRepository<PendingSpaceInvitationJpaEntity, Long> {
    Optional<PendingSpaceInvitationJpaEntity> findByToken(String token);

    /**
     * Las consultas derivadas de borrado se ejecutan desde un planificador y
     * por ello necesitan abrir explícitamente una transacción.
     */
    @Transactional
    long deleteByExpiresAtBefore(LocalDateTime dateTime);
}
