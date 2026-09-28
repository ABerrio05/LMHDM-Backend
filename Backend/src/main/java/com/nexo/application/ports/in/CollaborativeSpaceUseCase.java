package com.nexo.application.ports.in;

import com.nexo.domain.model.CollaborativeSpace;
import com.nexo.domain.model.SpaceMember;
import com.nexo.domain.model.Task;
import java.util.List;

public interface CollaborativeSpaceUseCase {
    CollaborativeSpace create(Long userId, CreateSpaceCommand command);
    List<CollaborativeSpace> listMine(Long userId);
    CollaborativeSpace get(Long userId, Long spaceId);
    SpaceMember invite(Long userId, Long spaceId, InviteMemberCommand command);
    List<SpaceMember> listMembers(Long userId, Long spaceId);
    List<Task> listTasks(Long userId, Long spaceId);

    record CreateSpaceCommand(String name, String description) { }
    record InviteMemberCommand(String email, SpaceMember.Role role) { }
}
