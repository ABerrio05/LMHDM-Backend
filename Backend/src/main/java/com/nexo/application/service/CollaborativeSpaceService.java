package com.nexo.application.service;

import com.nexo.application.ports.in.CollaborativeSpaceUseCase;
import com.nexo.application.ports.out.CollaborativeSpaceRepositoryPort;
import com.nexo.application.ports.out.SpaceMemberRepositoryPort;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.application.ports.out.UserRepositoryPort;
import com.nexo.application.ports.out.PendingSpaceInvitationRepositoryPort;
import com.nexo.domain.model.PendingSpaceInvitation;
import com.nexo.domain.model.CollaborativeSpace;
import com.nexo.domain.model.SpaceMember;
import com.nexo.domain.model.Task;
import com.nexo.domain.model.User;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Casos de uso para compartir listas de tareas sin depender de HTTP ni JPA. */
public class CollaborativeSpaceService implements CollaborativeSpaceUseCase {
    private static final Logger log = LoggerFactory.getLogger(CollaborativeSpaceService.class);
    private final CollaborativeSpaceRepositoryPort spaces;
    private final SpaceMemberRepositoryPort members;
    private final UserRepositoryPort users;
    private final TaskRepositoryPort tasks;
    private final ActivityEmailNotificationService activityEmails;
    private final PendingSpaceInvitationRepositoryPort pendingInvitations;
    private final String frontendUrl;

    public CollaborativeSpaceService(CollaborativeSpaceRepositoryPort spaces, SpaceMemberRepositoryPort members,
                                     UserRepositoryPort users, TaskRepositoryPort tasks) {
        this(spaces, members, users, tasks, null, null, "http://localhost:4200");
    }

    public CollaborativeSpaceService(CollaborativeSpaceRepositoryPort spaces, SpaceMemberRepositoryPort members,
                                     UserRepositoryPort users, TaskRepositoryPort tasks,
                                     ActivityEmailNotificationService activityEmails) {
        this(spaces, members, users, tasks, activityEmails, null, "http://localhost:4200");
    }

    public CollaborativeSpaceService(CollaborativeSpaceRepositoryPort spaces, SpaceMemberRepositoryPort members,
                                     UserRepositoryPort users, TaskRepositoryPort tasks, ActivityEmailNotificationService activityEmails,
                                     PendingSpaceInvitationRepositoryPort pendingInvitations, String frontendUrl) {
        this.spaces = spaces; this.members = members; this.users = users; this.tasks = tasks; this.activityEmails = activityEmails;
        this.pendingInvitations = pendingInvitations;
        this.frontendUrl = frontendUrl == null ? "http://localhost:4200" : frontendUrl.replaceAll("/+$", "");
    }

    @Override public CollaborativeSpace create(Long userId, CreateSpaceCommand command) {
        CollaborativeSpace space = spaces.save(new CollaborativeSpace(null, command.name(), command.description(), userId, LocalDateTime.now()));
        members.save(new SpaceMember(null, space.id(), userId, SpaceMember.Role.ADMINISTRADOR, LocalDateTime.now()));
        log.info("space_created spaceId={} creatorId={}", space.id(), userId);
        if (activityEmails != null) users.findById(userId).ifPresent(creator -> activityEmails.spaceCreated(creator, space));
        return space;
    }

    @Override public List<CollaborativeSpace> listMine(Long userId) {
        return spaces.findAllByIds(members.findAllByUserId(userId).stream().map(SpaceMember::spaceId).toList());
    }

    @Override public CollaborativeSpace get(Long userId, Long spaceId) {
        requireMembership(userId, spaceId);
        return spaces.findById(spaceId).orElseThrow(SpaceNotFoundException::new);
    }

    @Override public CollaborativeSpace update(Long userId, Long spaceId, UpdateSpaceCommand command) {
        requireAdministrator(userId, spaceId);
        CollaborativeSpace current = spaces.findById(spaceId).orElseThrow(SpaceNotFoundException::new);
        CollaborativeSpace updated = spaces.save(new CollaborativeSpace(current.id(), command.name(), command.description(),
                current.creatorId(), current.createdAt()));
        log.info("space_updated spaceId={} requestedBy={}", spaceId, userId);
        return updated;
    }

    @Override public void delete(Long userId, Long spaceId) {
        requireAdministrator(userId, spaceId);
        tasks.findAllBySpaceId(spaceId).forEach(task -> tasks.save(new Task(task.id(), task.title(), task.description(),
                task.dueDate(), task.priority(), task.status(), task.ownerId(), null, task.recurrenceId())));
        members.deleteAllBySpaceId(spaceId);
        spaces.deleteById(spaceId);
        log.info("space_deleted spaceId={} requestedBy={}", spaceId, userId);
    }

    @Override public InvitationResult invite(Long userId, Long spaceId, InviteMemberCommand command) {
        requireAdministrator(userId, spaceId);
        CollaborativeSpace space = spaces.findById(spaceId).orElseThrow(SpaceNotFoundException::new);
        User inviter = users.findById(userId).orElseThrow(UserNotFoundException::new);
        User invitedUser = users.findByEmail(command.email()).orElse(null);
        if (invitedUser == null) return createPendingInvitation(inviter, space, command);
        if (members.findBySpaceIdAndUserId(spaceId, invitedUser.id()).isPresent()) {
            throw new MemberAlreadyExistsException();
        }
        SpaceMember member = members.save(new SpaceMember(null, spaceId, invitedUser.id(), command.role(), LocalDateTime.now()));
        if (activityEmails != null) {
            activityEmails.spaceInvitation(invitedUser, inviter, space, command.role());
        }
        log.info("space_member_invited spaceId={} invitedUserId={} role={} requestedBy={}", spaceId,
                invitedUser.id(), command.role(), userId);
        return new InvitationResult(member, false);
    }

    private InvitationResult createPendingInvitation(User inviter, CollaborativeSpace space, InviteMemberCommand command) {
        if (pendingInvitations == null) throw new UserNotFoundException();
        LocalDateTime now = LocalDateTime.now();
        String token = newInvitationToken();
        PendingSpaceInvitation invitation = pendingInvitations.save(new PendingSpaceInvitation(null, space.id(), command.email(),
                command.role(), inviter.id(), token, now.plus(7, ChronoUnit.DAYS), now, null));
        if (activityEmails != null) {
            activityEmails.pendingSpaceInvitation(inviter, space, invitation.email(), invitation.role(),
                    frontendUrl + "/register?invitation=" + invitation.token());
        }
        log.info("space_registration_invited spaceId={} email={} requestedBy={}", space.id(), invitation.email(), inviter.id());
        return new InvitationResult(null, true);
    }

    private String newInvitationToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override public List<SpaceMember> listMembers(Long userId, Long spaceId) {
        requireMembership(userId, spaceId); return members.findAllBySpaceId(spaceId);
    }

    @Override public List<Task> listTasks(Long userId, Long spaceId) {
        requireMembership(userId, spaceId); return tasks.findAllBySpaceId(spaceId);
    }

    private SpaceMember requireMembership(Long userId, Long spaceId) {
        spaces.findById(spaceId).orElseThrow(SpaceNotFoundException::new);
        return members.findBySpaceIdAndUserId(spaceId, userId).orElseThrow(SpaceAccessDeniedException::new);
    }
    private void requireAdministrator(Long userId, Long spaceId) {
        if (requireMembership(userId, spaceId).role() != SpaceMember.Role.ADMINISTRADOR) {
            throw new SpaceAccessDeniedException();
        }
    }

    public static class SpaceNotFoundException extends RuntimeException { }
    public static class SpaceAccessDeniedException extends RuntimeException { }
    public static class UserNotFoundException extends RuntimeException { }
    public static class MemberAlreadyExistsException extends RuntimeException { }
}
