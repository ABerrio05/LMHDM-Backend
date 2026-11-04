package com.nexo.infrastructure.adapters.out.persistence;

import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPendingSpaceInvitationRepository extends JpaRepository<PendingSpaceInvitationJpaEntity, Long> {
    Optional<PendingSpaceInvitationJpaEntity> findByToken(String token);
    long deleteByExpiresAtBefore(LocalDateTime dateTime);
}
