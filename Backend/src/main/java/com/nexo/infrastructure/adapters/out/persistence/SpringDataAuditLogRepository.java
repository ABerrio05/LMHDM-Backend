package com.nexo.infrastructure.adapters.out.persistence;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAuditLogRepository extends JpaRepository<AuditLogJpaEntity, Long> {
    List<AuditLogJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
