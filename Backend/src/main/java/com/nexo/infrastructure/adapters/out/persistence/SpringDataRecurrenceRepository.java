package com.nexo.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataRecurrenceRepository extends JpaRepository<RecurrenceJpaEntity, Long> { }
