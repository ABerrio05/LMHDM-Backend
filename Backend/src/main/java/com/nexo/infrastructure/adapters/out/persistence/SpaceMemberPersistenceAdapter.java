package com.nexo.infrastructure.adapters.out.persistence;

import com.nexo.application.ports.out.SpaceMemberRepositoryPort;
import com.nexo.domain.model.SpaceMember;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SpaceMemberPersistenceAdapter implements SpaceMemberRepositoryPort {
    private final SpringDataSpaceMemberRepository repository;
    public SpaceMemberPersistenceAdapter(SpringDataSpaceMemberRepository repository) { this.repository = repository; }
    @Override public SpaceMember save(SpaceMember member) {
        LocalDateTime joinedAt = member.joinedAt() == null ? LocalDateTime.now() : member.joinedAt();
        SpaceMemberJpaEntity saved = repository.save(new SpaceMemberJpaEntity(member.spaceId(), member.userId(),
                member.role().name().toLowerCase(Locale.ROOT), joinedAt));
        return toDomain(saved);
    }
    @Override public Optional<SpaceMember> findBySpaceIdAndUserId(Long spaceId, Long userId) {
        return repository.findBySpaceIdAndUserId(spaceId, userId).map(this::toDomain);
    }
    @Override public List<SpaceMember> findAllBySpaceId(Long spaceId) {
        return repository.findAllBySpaceIdOrderByJoinedAtAsc(spaceId).stream().map(this::toDomain).toList();
    }
    @Override public List<SpaceMember> findAllByUserId(Long userId) { return repository.findAllByUserId(userId).stream().map(this::toDomain).toList(); }
    private SpaceMember toDomain(SpaceMemberJpaEntity entity) {
        return new SpaceMember(entity.getId(), entity.getSpaceId(), entity.getUserId(),
                SpaceMember.Role.valueOf(entity.getRole().toUpperCase(Locale.ROOT)), entity.getJoinedAt());
    }
}
