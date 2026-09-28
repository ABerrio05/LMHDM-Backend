package com.nexo.infrastructure.adapters.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCollaborativeSpaceRepository extends JpaRepository<CollaborativeSpaceJpaEntity, Long> {
    List<CollaborativeSpaceJpaEntity> findAllByIdInOrderByCreatedAtDesc(List<Long> ids);
}
