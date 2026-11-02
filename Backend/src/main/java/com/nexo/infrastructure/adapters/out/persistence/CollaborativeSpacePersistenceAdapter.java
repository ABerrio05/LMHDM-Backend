package com.nexo.infrastructure.adapters.out.persistence;

import com.nexo.application.ports.out.CollaborativeSpaceRepositoryPort;
import com.nexo.domain.model.CollaborativeSpace;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CollaborativeSpacePersistenceAdapter implements CollaborativeSpaceRepositoryPort {
    private final SpringDataCollaborativeSpaceRepository repository;
    public CollaborativeSpacePersistenceAdapter(SpringDataCollaborativeSpaceRepository repository) { this.repository = repository; }
    @Override public CollaborativeSpace save(CollaborativeSpace space) {
        if (space.id() != null) {
            CollaborativeSpaceJpaEntity existing = repository.findById(space.id()).orElseThrow();
            existing.update(space.name(), space.description());
            return toDomain(repository.save(existing));
        }
        CollaborativeSpaceJpaEntity saved = repository.save(new CollaborativeSpaceJpaEntity(
                space.name(), space.description(), space.creatorId(), space.createdAt()));
        return toDomain(saved);
    }
    @Override public void deleteById(Long spaceId) { repository.deleteById(spaceId); }
    @Override public Optional<CollaborativeSpace> findById(Long spaceId) { return repository.findById(spaceId).map(this::toDomain); }
    @Override public List<CollaborativeSpace> findAllByIds(List<Long> spaceIds) {
        if (spaceIds.isEmpty()) return List.of();
        return repository.findAllByIdInOrderByCreatedAtDesc(spaceIds).stream().map(this::toDomain).toList();
    }
    private CollaborativeSpace toDomain(CollaborativeSpaceJpaEntity entity) {
        return new CollaborativeSpace(entity.getId(), entity.getName(), entity.getDescription(), entity.getCreatorId(), entity.getCreatedAt());
    }
}
