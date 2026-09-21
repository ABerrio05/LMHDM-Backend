package com.nexo.infrastructure.adapters.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataTaskRepository extends JpaRepository<TaskJpaEntity, Long> {
    List<TaskJpaEntity> findAllByOwnerIdOrderByDueDateAsc(Long ownerId);
}
