package com.nexo.application.ports.in;

import com.nexo.domain.model.CollaborativeSpace;
import com.nexo.domain.model.SpaceMember;
import com.nexo.domain.model.Task;
import java.util.List;

public interface CollaborativeSpaceUseCase {
    CollaborativeSpace create(Long userId, CreateSpaceCommand command);
    List<CollaborativeSpace> listMine(Long userId);
    CollaborativeSpace get(Long userId, Long spaceId);
    CollaborativeSpace update(Long userId, Long spaceId, UpdateSpaceCommand command);
    void delete(Long userId, Long spaceId);
    InvitationResult invite(Long userId, Long spaceId, InviteMemberCommand command);
    List<SpaceMember> listMembers(Long userId, Long spaceId);
    List<MemberView> listMemberViews(Long userId, Long spaceId);
    SpaceMember updateMemberRole(Long userId, Long spaceId, Long memberUserId, SpaceMember.Role role);
    Task assignTask(Long userId, Long spaceId, Long taskId, Long assigneeId);
    List<Task> listTasks(Long userId, Long spaceId);

    record CreateSpaceCommand(String name, String description) { }
    record UpdateSpaceCommand(String name, String description) { }
    record InviteMemberCommand(String email, SpaceMember.Role role) { }
    record InvitationResult(SpaceMember member, boolean registrationRequired) { }
    record MemberView(Long id, Long userId, String userName, SpaceMember.Role role, java.time.LocalDateTime joinedAt) { }
}
