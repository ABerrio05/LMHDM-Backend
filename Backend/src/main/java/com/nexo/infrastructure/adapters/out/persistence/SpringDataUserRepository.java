package com.nexo.infrastructure.adapters.out.persistence;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {
    boolean existsByEmail(String email);
    Optional<UserJpaEntity> findByEmail(String email);
    List<UserJpaEntity> findAllByOrderByRegisteredAtDesc();
}
