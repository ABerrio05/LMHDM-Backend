package com.nexo.infrastructure.adapters.out.persistence;

import com.nexo.application.ports.out.PendingSpaceInvitationRepositoryPort;
import com.nexo.domain.model.PendingSpaceInvitation;
import com.nexo.domain.model.SpaceMember;
import java.util.Locale;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class PendingSpaceInvitationPersistenceAdapter implements PendingSpaceInvitationRepositoryPort {
    private final SpringDataPendingSpaceInvitationRepository repository;
    public PendingSpaceInvitationPersistenceAdapter(SpringDataPendingSpaceInvitationRepository repository) { this.repository = repository; }
    @Override public PendingSpaceInvitation save(PendingSpaceInvitation invitation) {
        PendingSpaceInvitationJpaEntity entity;
        if (invitation.id() == null) {
            entity = new PendingSpaceInvitationJpaEntity(invitation.spaceId(), invitation.email(), invitation.role().name().toLowerCase(Locale.ROOT),
                    invitation.inviterId(), invitation.token(), invitation.expiresAt(), invitation.createdAt(), invitation.acceptedAt());
        } else {
            entity = repository.findById(invitation.id()).orElseThrow();
            if (invitation.acceptedAt() != null && entity.getAcceptedAt() == null) entity.accept(invitation.acceptedAt());
        }
        return toDomain(repository.save(entity));
    }
    @Override public Optional<PendingSpaceInvitation> findByToken(String token) { return repository.findByToken(token).map(this::toDomain); }
    @Override public long deleteExpiredBefore(LocalDateTime dateTime) { return repository.deleteByExpiresAtBefore(dateTime); }
    private PendingSpaceInvitation toDomain(PendingSpaceInvitationJpaEntity entity) {
        return new PendingSpaceInvitation(entity.getId(), entity.getSpaceId(), entity.getEmail(),
                SpaceMember.Role.valueOf(entity.getRole().toUpperCase(Locale.ROOT)), entity.getInviterId(), entity.getToken(),
                entity.getExpiresAt(), entity.getCreatedAt(), entity.getAcceptedAt());
    }
}
