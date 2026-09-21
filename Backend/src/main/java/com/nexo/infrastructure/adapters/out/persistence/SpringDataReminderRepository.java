package com.nexo.infrastructure.adapters.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataReminderRepository extends JpaRepository<ReminderJpaEntity, Long> {
    List<ReminderJpaEntity> findAllByTaskIdOrderByScheduledAtAsc(Long taskId);
}
