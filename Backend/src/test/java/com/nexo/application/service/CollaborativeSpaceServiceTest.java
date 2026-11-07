package com.nexo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nexo.application.ports.in.CollaborativeSpaceUseCase;
import com.nexo.application.ports.out.CollaborativeSpaceRepositoryPort;
import com.nexo.application.ports.out.PendingSpaceInvitationRepositoryPort;
import com.nexo.application.ports.out.SpaceMemberRepositoryPort;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.application.ports.out.UserRepositoryPort;
import com.nexo.domain.model.CollaborativeSpace;
import com.nexo.domain.model.PendingSpaceInvitation;
import com.nexo.domain.model.SpaceMember;
import com.nexo.domain.model.Task;
import com.nexo.domain.model.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CollaborativeSpaceServiceTest {
    @Test
    void creatorBecomesAdministratorAndCanInviteRegisteredUser() {
        InMemorySpaceRepository spaces = new InMemorySpaceRepository();
        InMemoryMemberRepository members = new InMemoryMemberRepository();
        User owner = new User(1L, "Ana", "ana@nexo.test", "hash", LocalDateTime.now());
        User invited = new User(2L, "Luis", "luis@nexo.test", "hash", LocalDateTime.now());
        CollaborativeSpaceService service = new CollaborativeSpaceService(spaces, members,
                new InMemoryUserRepository(List.of(owner, invited)), new EmptyTaskRepository());

        CollaborativeSpace space = service.create(owner.id(),
                new CollaborativeSpaceUseCase.CreateSpaceCommand("Comité", "Preparación"));
        var invitation = service.invite(owner.id(), space.id(),
                new CollaborativeSpaceUseCase.InviteMemberCommand(invited.email(), SpaceMember.Role.MIEMBRO));

        assertEquals(SpaceMember.Role.ADMINISTRADOR,
                members.findBySpaceIdAndUserId(space.id(), owner.id()).orElseThrow().role());
        assertEquals(invited.id(), invitation.member().userId());
        assertTrue(!invitation.registrationRequired());
        assertEquals(2, service.listMembers(owner.id(), space.id()).size());
    }

    @Test
    void regularMemberCanInviteAnotherUser() {
        InMemorySpaceRepository spaces = new InMemorySpaceRepository();
        InMemoryMemberRepository members = new InMemoryMemberRepository();
        User owner = new User(1L, "Ana", "ana@nexo.test", "hash", LocalDateTime.now());
        User member = new User(2L, "Luis", "luis@nexo.test", "hash", LocalDateTime.now());
        User third = new User(3L, "Eva", "eva@nexo.test", "hash", LocalDateTime.now());
        CollaborativeSpaceService service = new CollaborativeSpaceService(spaces, members,
                new InMemoryUserRepository(List.of(owner, member, third)), new EmptyTaskRepository());
        CollaborativeSpace space = service.create(owner.id(),
                new CollaborativeSpaceUseCase.CreateSpaceCommand("Comité", null));
        members.save(new SpaceMember(null, space.id(), member.id(), SpaceMember.Role.MIEMBRO, LocalDateTime.now()));

        var invitation = service.invite(member.id(), space.id(),
                new CollaborativeSpaceUseCase.InviteMemberCommand(third.email(), SpaceMember.Role.MIEMBRO));

        assertEquals(third.id(), invitation.member().userId());
        assertEquals(SpaceMember.Role.MIEMBRO, invitation.member().role());
    }

    @Test
    void administratorCanUpdateAndDeleteSpaceWithoutDeletingTasks() {
        InMemorySpaceRepository spaces = new InMemorySpaceRepository();
        InMemoryMemberRepository members = new InMemoryMemberRepository();
        User owner = new User(1L, "Ana", "ana@nexo.test", "hash", LocalDateTime.now());
        InMemoryTaskRepository tasks = new InMemoryTaskRepository(List.of(
                new Task(10L, "Compartida", "", null, Task.Priority.MEDIA, Task.TaskStatus.PENDIENTE, owner.id(), 1L, null)));
        CollaborativeSpaceService service = new CollaborativeSpaceService(spaces, members,
                new InMemoryUserRepository(List.of(owner)), tasks);
        CollaborativeSpace space = service.create(owner.id(), new CollaborativeSpaceUseCase.CreateSpaceCommand("Inicial", "Primera"));

        CollaborativeSpace updated = service.update(owner.id(), space.id(),
                new CollaborativeSpaceUseCase.UpdateSpaceCommand("Actualizado", "Segunda"));
        service.delete(owner.id(), space.id());

        assertEquals("Actualizado", updated.name());
        assertTrue(spaces.findById(space.id()).isEmpty());
        assertTrue(members.findAllBySpaceId(space.id()).isEmpty());
        assertEquals(null, tasks.findById(10L).orElseThrow().spaceId());
    }

    @Test
    void createsPendingInvitationForUnregisteredUserWithTwoHourExpiry() {
        InMemorySpaceRepository spaces = new InMemorySpaceRepository();
        InMemoryMemberRepository members = new InMemoryMemberRepository();
        InMemoryPendingInvitationRepository invitations = new InMemoryPendingInvitationRepository();
        User owner = new User(1L, "Ana", "ana@nexo.test", "hash", LocalDateTime.now());
        CollaborativeSpaceService service = new CollaborativeSpaceService(
                spaces, members, new InMemoryUserRepository(List.of(owner)), new EmptyTaskRepository(),
                null, invitations, "http://frontend.test");
        CollaborativeSpace space = service.create(owner.id(),
                new CollaborativeSpaceUseCase.CreateSpaceCommand("Comité", null));

        var result = service.invite(owner.id(), space.id(),
                new CollaborativeSpaceUseCase.InviteMemberCommand("nueva@nexo.test", SpaceMember.Role.MIEMBRO));

        PendingSpaceInvitation invitation = invitations.values.getFirst();
        assertTrue(result.registrationRequired());
        assertNull(result.member());
        assertEquals("nueva@nexo.test", invitation.email());
        assertEquals(space.id(), invitation.spaceId());
        assertEquals(SpaceMember.Role.MIEMBRO, invitation.role());
        assertNotNull(invitation.token());
        assertTrue(invitation.isUsableAt(invitation.createdAt().plusMinutes(1)));
        assertFalse(invitation.isUsableAt(invitation.expiresAt()));
    }

    @Test
    void memberCannotGrantManagementRoleAndGuestCannotInvite() {
        InMemorySpaceRepository spaces = new InMemorySpaceRepository();
        InMemoryMemberRepository members = new InMemoryMemberRepository();
        User owner = new User(1L, "Ana", "ana@nexo.test", "hash", LocalDateTime.now());
        User member = new User(2L, "Luis", "luis@nexo.test", "hash", LocalDateTime.now());
        User guest = new User(3L, "Eva", "eva@nexo.test", "hash", LocalDateTime.now());
        User candidate = new User(4L, "Teo", "teo@nexo.test", "hash", LocalDateTime.now());
        CollaborativeSpaceService service = new CollaborativeSpaceService(spaces, members,
                new InMemoryUserRepository(List.of(owner, member, guest, candidate)), new EmptyTaskRepository());
        CollaborativeSpace space = service.create(owner.id(),
                new CollaborativeSpaceUseCase.CreateSpaceCommand("Comité", null));
        members.save(new SpaceMember(null, space.id(), member.id(), SpaceMember.Role.MIEMBRO, LocalDateTime.now()));
        members.save(new SpaceMember(null, space.id(), guest.id(), SpaceMember.Role.INVITADO, LocalDateTime.now()));

        assertThrows(CollaborativeSpaceService.SpaceAccessDeniedException.class, () -> service.invite(member.id(), space.id(),
                new CollaborativeSpaceUseCase.InviteMemberCommand(candidate.email(), SpaceMember.Role.MODERADOR)));
        assertThrows(CollaborativeSpaceService.SpaceAccessDeniedException.class, () -> service.invite(guest.id(), space.id(),
                new CollaborativeSpaceUseCase.InviteMemberCommand(candidate.email(), SpaceMember.Role.MIEMBRO)));
    }

    @Test
    void managerCanChangeRoleAndAssignSharedTask() {
        InMemorySpaceRepository spaces = new InMemorySpaceRepository();
        InMemoryMemberRepository members = new InMemoryMemberRepository();
        User owner = new User(1L, "Ana", "ana@nexo.test", "hash", LocalDateTime.now());
        User teammate = new User(2L, "Luis", "luis@nexo.test", "hash", LocalDateTime.now());
        InMemoryTaskRepository tasks = new InMemoryTaskRepository(List.of(
                new Task(10L, "Compartida", "", null, Task.Priority.MEDIA, Task.TaskStatus.PENDIENTE, owner.id(), 1L, null)));
        CollaborativeSpaceService service = new CollaborativeSpaceService(spaces, members,
                new InMemoryUserRepository(List.of(owner, teammate)), tasks);
        CollaborativeSpace space = service.create(owner.id(),
                new CollaborativeSpaceUseCase.CreateSpaceCommand("Comité", null));
        members.save(new SpaceMember(null, space.id(), teammate.id(), SpaceMember.Role.MIEMBRO, LocalDateTime.now()));

        SpaceMember changed = service.updateMemberRole(owner.id(), space.id(), teammate.id(), SpaceMember.Role.MODERADOR);
        Task assigned = service.assignTask(owner.id(), space.id(), 10L, teammate.id());

        assertEquals(SpaceMember.Role.MODERADOR, changed.role());
        assertEquals(teammate.id(), assigned.ownerId());
        assertEquals(teammate.id(), tasks.findById(10L).orElseThrow().ownerId());
    }

    @Test
    void invitedUsersCannotAssignSharedTasks() {
        InMemorySpaceRepository spaces = new InMemorySpaceRepository();
        InMemoryMemberRepository members = new InMemoryMemberRepository();
        User owner = new User(1L, "Ana", "ana@nexo.test", "hash", LocalDateTime.now());
        User guest = new User(2L, "Eva", "eva@nexo.test", "hash", LocalDateTime.now());
        InMemoryTaskRepository tasks = new InMemoryTaskRepository(List.of(
                new Task(10L, "Compartida", "", null, Task.Priority.MEDIA, Task.TaskStatus.PENDIENTE, owner.id(), 1L, null)));
        CollaborativeSpaceService service = new CollaborativeSpaceService(spaces, members,
                new InMemoryUserRepository(List.of(owner, guest)), tasks);
        CollaborativeSpace space = service.create(owner.id(),
                new CollaborativeSpaceUseCase.CreateSpaceCommand("Comité", null));
        members.save(new SpaceMember(null, space.id(), guest.id(), SpaceMember.Role.INVITADO, LocalDateTime.now()));

        assertThrows(CollaborativeSpaceService.SpaceAccessDeniedException.class,
                () -> service.assignTask(guest.id(), space.id(), 10L, owner.id()));
    }

    private static class InMemorySpaceRepository implements CollaborativeSpaceRepositoryPort {
        private final List<CollaborativeSpace> values = new ArrayList<>();
        public CollaborativeSpace save(CollaborativeSpace space) {
            if (space.id() != null) {
                values.removeIf(current -> current.id().equals(space.id()));
                values.add(space);
                return space;
            }
            CollaborativeSpace saved = new CollaborativeSpace((long) values.size() + 1, space.name(), space.description(),
                    space.creatorId(), space.createdAt());
            values.add(saved); return saved;
        }
        public Optional<CollaborativeSpace> findById(Long id) { return values.stream().filter(space -> space.id().equals(id)).findFirst(); }
        public List<CollaborativeSpace> findAllByIds(List<Long> ids) { return values.stream().filter(space -> ids.contains(space.id())).toList(); }
        public void deleteById(Long id) { values.removeIf(space -> space.id().equals(id)); }
    }

    private static class InMemoryMemberRepository implements SpaceMemberRepositoryPort {
        private final List<SpaceMember> values = new ArrayList<>();
        public SpaceMember save(SpaceMember member) {
            long id = member.id() == null ? (long) values.size() + 1 : member.id();
            values.removeIf(current -> current.id().equals(id));
            SpaceMember saved = new SpaceMember(id, member.spaceId(), member.userId(), member.role(), member.joinedAt());
            values.add(saved); return saved;
        }
        public Optional<SpaceMember> findBySpaceIdAndUserId(Long spaceId, Long userId) {
            return values.stream().filter(member -> member.spaceId().equals(spaceId) && member.userId().equals(userId)).findFirst();
        }
        public List<SpaceMember> findAllBySpaceId(Long spaceId) { return values.stream().filter(member -> member.spaceId().equals(spaceId)).toList(); }
        public List<SpaceMember> findAllByUserId(Long userId) { return values.stream().filter(member -> member.userId().equals(userId)).toList(); }
        public SpaceMember updateRole(Long spaceId, Long userId, SpaceMember.Role role) {
            SpaceMember current = findBySpaceIdAndUserId(spaceId, userId).orElseThrow();
            return save(new SpaceMember(current.id(), current.spaceId(), current.userId(), role, current.joinedAt()));
        }
        public void deleteAllBySpaceId(Long spaceId) { values.removeIf(member -> member.spaceId().equals(spaceId)); }
    }

    private static class InMemoryPendingInvitationRepository implements PendingSpaceInvitationRepositoryPort {
        private final List<PendingSpaceInvitation> values = new ArrayList<>();
        public PendingSpaceInvitation save(PendingSpaceInvitation invitation) {
            PendingSpaceInvitation saved = new PendingSpaceInvitation((long) values.size() + 1,
                    invitation.spaceId(), invitation.email(), invitation.role(), invitation.inviterId(), invitation.token(),
                    invitation.expiresAt(), invitation.createdAt(), invitation.acceptedAt());
            values.add(saved);
            return saved;
        }
        public Optional<PendingSpaceInvitation> findByToken(String token) {
            return values.stream().filter(invitation -> invitation.token().equals(token)).findFirst();
        }
        public long deleteExpiredBefore(LocalDateTime dateTime) {
            long before = values.size();
            values.removeIf(invitation -> !invitation.expiresAt().isAfter(dateTime));
            return before - values.size();
        }
    }

    private record InMemoryUserRepository(List<User> users) implements UserRepositoryPort {
        public boolean existsByEmail(String email) { return users.stream().anyMatch(user -> user.email().equals(email)); }
        public Optional<User> findByEmail(String email) { return users.stream().filter(user -> user.email().equals(email)).findFirst(); }
        public Optional<User> findById(Long userId) { return users.stream().filter(user -> user.id().equals(userId)).findFirst(); }
        public User save(User user) { return user; }
    }

    private static class EmptyTaskRepository implements TaskRepositoryPort {
        public Task save(Task task) { return task; }
        public List<Task> findAllByOwnerId(Long ownerId) { return List.of(); }
        public List<Task> findAllBySpaceId(Long spaceId) { return List.of(); }
        public Optional<Task> findById(Long taskId) { return Optional.empty(); }
        public void delete(Task task) { }
    }

    private static class InMemoryTaskRepository extends EmptyTaskRepository {
        private final List<Task> values;
        InMemoryTaskRepository(List<Task> values) { this.values = new ArrayList<>(values); }
        @Override public Task save(Task task) { values.removeIf(current -> current.id().equals(task.id())); values.add(task); return task; }
        @Override public List<Task> findAllBySpaceId(Long spaceId) { return values.stream().filter(task -> spaceId.equals(task.spaceId())).toList(); }
        @Override public Optional<Task> findById(Long id) { return values.stream().filter(task -> task.id().equals(id)).findFirst(); }
    }
}
