package com.nexo.application.service;

import com.nexo.application.ports.in.CollaborativeSpaceUseCase;
import com.nexo.application.ports.out.CollaborativeSpaceRepositoryPort;
import com.nexo.application.ports.out.SpaceMemberRepositoryPort;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.application.ports.out.UserRepositoryPort;
import com.nexo.domain.model.CollaborativeSpace;
import com.nexo.domain.model.SpaceMember;
import com.nexo.domain.model.Task;
import com.nexo.domain.model.User;
import java.time.LocalDateTime;
import java.util.List;

/** Casos de uso para compartir listas de tareas sin depender de HTTP ni JPA. */
public class CollaborativeSpaceService implements CollaborativeSpaceUseCase {
    private final CollaborativeSpaceRepositoryPort spaces;
    private final SpaceMemberRepositoryPort members;
    private final UserRepositoryPort users;
    private final TaskRepositoryPort tasks;

    public CollaborativeSpaceService(CollaborativeSpaceRepositoryPort spaces, SpaceMemberRepositoryPort members,
                                     UserRepositoryPort users, TaskRepositoryPort tasks) {
        this.spaces = spaces; this.members = members; this.users = users; this.tasks = tasks;
    }

    @Override public CollaborativeSpace create(Long userId, CreateSpaceCommand command) {
        CollaborativeSpace space = spaces.save(new CollaborativeSpace(null, command.name(), command.description(), userId, LocalDateTime.now()));
        members.save(new SpaceMember(null, space.id(), userId, SpaceMember.Role.ADMINISTRADOR, LocalDateTime.now()));
        return space;
    }

    @Override public List<CollaborativeSpace> listMine(Long userId) {
        return spaces.findAllByIds(members.findAllByUserId(userId).stream().map(SpaceMember::spaceId).toList());
    }

    @Override public CollaborativeSpace get(Long userId, Long spaceId) {
        requireMembership(userId, spaceId);
        return spaces.findById(spaceId).orElseThrow(SpaceNotFoundException::new);
    }

    @Override public SpaceMember invite(Long userId, Long spaceId, InviteMemberCommand command) {
        requireAdministrator(userId, spaceId);
        User invitedUser = users.findByEmail(command.email()).orElseThrow(UserNotFoundException::new);
        if (members.findBySpaceIdAndUserId(spaceId, invitedUser.id()).isPresent()) {
            throw new MemberAlreadyExistsException();
        }
        return members.save(new SpaceMember(null, spaceId, invitedUser.id(), command.role(), LocalDateTime.now()));
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
