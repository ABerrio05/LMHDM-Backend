package com.nexo.infrastructure.scheduling;

import com.nexo.application.ports.out.PendingSpaceInvitationRepositoryPort;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Elimina invitaciones vencidas para que el registro temporal no crezca indefinidamente. */
@Component
public class PendingSpaceInvitationCleanupScheduler {
    private static final Logger log = LoggerFactory.getLogger(PendingSpaceInvitationCleanupScheduler.class);
    private final PendingSpaceInvitationRepositoryPort invitations;

    public PendingSpaceInvitationCleanupScheduler(PendingSpaceInvitationRepositoryPort invitations) { this.invitations = invitations; }

    @Scheduled(fixedDelayString = "${app.invitations.cleanup-delay-ms:3600000}", initialDelay = 60000)
    public void removeExpiredInvitations() {
        long deleted = invitations.deleteExpiredBefore(LocalDateTime.now());
        if (deleted > 0) log.info("expired_space_invitations_deleted count={}", deleted);
    }
}
