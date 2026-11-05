package com.nexo.infrastructure.adapters.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

interface SpringDataEmailNotificationRepository extends JpaRepository<EmailNotificationJpaEntity, Long> {
    Optional<EmailNotificationJpaEntity> findByReminderId(Long reminderId);

    /**
     * A derived delete query needs an explicit write transaction.  Without it,
     * JPA rejects the remove operation and leaves the reminder (and its task)
     * impossible to delete once an email notification exists.
     */
    @Modifying
    @Transactional
    @Query("delete from EmailNotificationJpaEntity notification where notification.reminderId = :reminderId")
    void deleteByReminderId(@Param("reminderId") Long reminderId);
    long countByStatus(String status);
}
