package com.nexo.application.ports.out;

import com.nexo.domain.model.PendingSpaceInvitation;
import java.util.Optional;
import java.time.LocalDateTime;

public interface PendingSpaceInvitationRepositoryPort {
    PendingSpaceInvitation save(PendingSpaceInvitation invitation);
    Optional<PendingSpaceInvitation> findByToken(String token);
    long deleteExpiredBefore(LocalDateTime dateTime);
}
