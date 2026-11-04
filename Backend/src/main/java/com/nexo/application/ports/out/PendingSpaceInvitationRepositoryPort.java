package com.nexo.application.ports.out;

import com.nexo.domain.model.PendingSpaceInvitation;
import java.util.Optional;

public interface PendingSpaceInvitationRepositoryPort {
    PendingSpaceInvitation save(PendingSpaceInvitation invitation);
    Optional<PendingSpaceInvitation> findByToken(String token);
}
