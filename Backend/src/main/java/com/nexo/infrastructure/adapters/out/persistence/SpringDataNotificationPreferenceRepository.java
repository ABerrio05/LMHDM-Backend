package com.nexo.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataNotificationPreferenceRepository extends JpaRepository<NotificationPreferenceJpaEntity, Long> { }
