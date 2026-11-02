package com.nexo.infrastructure.adapters.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataEmailNotificationRepository extends JpaRepository<EmailNotificationJpaEntity, Long> {
    Optional<EmailNotificationJpaEntity> findByReminderId(Long reminderId);
    void deleteByReminderId(Long reminderId);
    long countByStatus(String status);
}
