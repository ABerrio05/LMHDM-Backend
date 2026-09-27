package com.nexo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        SpaceMember membership = service.invite(owner.id(), space.id(),
                new CollaborativeSpaceUseCase.InviteMemberCommand(invited.email(), SpaceMember.Role.MIEMBRO));

        assertEquals(SpaceMember.Role.ADMINISTRADOR,
                members.findBySpaceIdAndUserId(space.id(), owner.id()).orElseThrow().role());
        assertEquals(invited.id(), membership.userId());
        assertEquals(2, service.listMembers(owner.id(), space.id()).size());
    }

    @Test
    void regularMemberCannotInviteAnotherUser() {
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

        assertThrows(CollaborativeSpaceService.SpaceAccessDeniedException.class, () -> service.invite(member.id(),
                space.id(), new CollaborativeSpaceUseCase.InviteMemberCommand(third.email(), SpaceMember.Role.MIEMBRO)));
    }

    private static class InMemorySpaceRepository implements CollaborativeSpaceRepositoryPort {
        private final List<CollaborativeSpace> values = new ArrayList<>();
        public CollaborativeSpace save(CollaborativeSpace space) {
            CollaborativeSpace saved = new CollaborativeSpace((long) values.size() + 1, space.name(), space.description(),
                    space.creatorId(), space.createdAt());
            values.add(saved); return saved;
        }
        public Optional<CollaborativeSpace> findById(Long id) { return values.stream().filter(space -> space.id().equals(id)).findFirst(); }
        public List<CollaborativeSpace> findAllByIds(List<Long> ids) { return values.stream().filter(space -> ids.contains(space.id())).toList(); }
    }

    private static class InMemoryMemberRepository implements SpaceMemberRepositoryPort {
        private final List<SpaceMember> values = new ArrayList<>();
        public SpaceMember save(SpaceMember member) {
            SpaceMember saved = new SpaceMember((long) values.size() + 1, member.spaceId(), member.userId(), member.role(), member.joinedAt());
            values.add(saved); return saved;
        }
        public Optional<SpaceMember> findBySpaceIdAndUserId(Long spaceId, Long userId) {
            return values.stream().filter(member -> member.spaceId().equals(spaceId) && member.userId().equals(userId)).findFirst();
        }
        public List<SpaceMember> findAllBySpaceId(Long spaceId) { return values.stream().filter(member -> member.spaceId().equals(spaceId)).toList(); }
        public List<SpaceMember> findAllByUserId(Long userId) { return values.stream().filter(member -> member.userId().equals(userId)).toList(); }
    }

    private record InMemoryUserRepository(List<User> users) implements UserRepositoryPort {
        public boolean existsByEmail(String email) { return users.stream().anyMatch(user -> user.email().equals(email)); }
        public Optional<User> findByEmail(String email) { return users.stream().filter(user -> user.email().equals(email)).findFirst(); }
        public User save(User user) { return user; }
    }

    private static class EmptyTaskRepository implements TaskRepositoryPort {
        public Task save(Task task) { return task; }
        public List<Task> findAllByOwnerId(Long ownerId) { return List.of(); }
        public List<Task> findAllBySpaceId(Long spaceId) { return List.of(); }
        public Optional<Task> findById(Long taskId) { return Optional.empty(); }
        public void delete(Task task) { }
    }
}
