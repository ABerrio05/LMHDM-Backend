package com.nexo.infrastructure.adapters.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataSpaceMemberRepository extends JpaRepository<SpaceMemberJpaEntity, Long> {
    Optional<SpaceMemberJpaEntity> findBySpaceIdAndUserId(Long spaceId, Long userId);
    List<SpaceMemberJpaEntity> findAllBySpaceIdOrderByJoinedAtAsc(Long spaceId);
    List<SpaceMemberJpaEntity> findAllByUserId(Long userId);
}
