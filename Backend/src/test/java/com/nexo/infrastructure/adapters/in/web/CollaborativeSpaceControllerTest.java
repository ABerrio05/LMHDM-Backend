package com.nexo.infrastructure.adapters.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexo.application.ports.in.CollaborativeSpaceUseCase;
import com.nexo.application.ports.in.CollaborativeSpaceUseCase.CreateSpaceCommand;
import com.nexo.application.ports.in.CollaborativeSpaceUseCase.InvitationResult;
import com.nexo.application.ports.in.CollaborativeSpaceUseCase.InviteMemberCommand;
import com.nexo.application.ports.in.CollaborativeSpaceUseCase.MemberView;
import com.nexo.application.ports.in.CollaborativeSpaceUseCase.UpdateSpaceCommand;
import com.nexo.domain.model.CollaborativeSpace;
import com.nexo.domain.model.SpaceMember;
import com.nexo.domain.model.Task;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CollaborativeSpaceControllerTest {

    private static final Long USER_ID = 7L;
    private static final Long SPACE_ID = 19L;
    private static final Long MEMBER_ID = 11L;
    private static final Long TASK_ID = 31L;

    @Test
    void delegatesEverySpaceOperationToTheUseCase() {
        CollaborativeSpaceUseCase useCase = mock(CollaborativeSpaceUseCase.class);
        LocalDateTime now = LocalDateTime.of(2026, 11, 6, 10, 0);
        CollaborativeSpace space = new CollaborativeSpace(SPACE_ID, "Equipo", "Entrega final", USER_ID, now);
        SpaceMember member = new SpaceMember(MEMBER_ID, SPACE_ID, USER_ID, SpaceMember.Role.ADMINISTRADOR, now);
        Task task = new Task(TASK_ID, "Preparar demo", "Presentación", now.plusDays(2),
                Task.Priority.ALTA, Task.TaskStatus.PENDIENTE, USER_ID, SPACE_ID, null);
        MemberView view = new MemberView(MEMBER_ID, USER_ID, "Andrés Berrio",
                SpaceMember.Role.ADMINISTRADOR, now);

        when(useCase.create(eq(USER_ID), any())).thenReturn(space);
        when(useCase.listMine(USER_ID)).thenReturn(List.of(space));
        when(useCase.get(USER_ID, SPACE_ID)).thenReturn(space);
        when(useCase.update(eq(USER_ID), eq(SPACE_ID), any())).thenReturn(space);
        when(useCase.invite(eq(USER_ID), eq(SPACE_ID), any())).thenReturn(new InvitationResult(member, false));
        when(useCase.listMemberViews(USER_ID, SPACE_ID)).thenReturn(List.of(view));
        when(useCase.updateMemberRole(USER_ID, SPACE_ID, MEMBER_ID, SpaceMember.Role.MODERADOR)).thenReturn(member);
        when(useCase.assignTask(USER_ID, SPACE_ID, TASK_ID, MEMBER_ID)).thenReturn(task);
        when(useCase.listTasks(USER_ID, SPACE_ID)).thenReturn(List.of(task));

        CollaborativeSpaceController controller = new CollaborativeSpaceController(useCase);
        CollaborativeSpaceController.CreateSpaceRequest request =
                new CollaborativeSpaceController.CreateSpaceRequest("Equipo", "Entrega final");

        var response = controller.create(USER_ID, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(space, response.getBody());
        assertEquals(List.of(space), controller.list(USER_ID));
        assertEquals(space, controller.get(USER_ID, SPACE_ID));
        assertEquals(space, controller.update(USER_ID, SPACE_ID, request));
        assertEquals(HttpStatus.NO_CONTENT, controller.delete(USER_ID, SPACE_ID).getStatusCode());

        CollaborativeSpaceController.InviteMemberRequest invite =
                new CollaborativeSpaceController.InviteMemberRequest("equipo@nexo.test", SpaceMember.Role.MIEMBRO);
        assertEquals(HttpStatus.CREATED, controller.invite(USER_ID, SPACE_ID, invite).getStatusCode());
        assertEquals(List.of(new CollaborativeSpaceController.MemberResponse(
                MEMBER_ID, USER_ID, "Andrés Berrio", SpaceMember.Role.ADMINISTRADOR, now)),
                controller.members(USER_ID, SPACE_ID));
        assertEquals(member, controller.updateRole(USER_ID, SPACE_ID, MEMBER_ID,
                new CollaborativeSpaceController.UpdateRoleRequest(SpaceMember.Role.MODERADOR)));
        assertEquals(task, controller.assignTask(USER_ID, SPACE_ID, TASK_ID,
                new CollaborativeSpaceController.AssignTaskRequest(MEMBER_ID)));
        assertEquals(List.of(task), controller.tasks(USER_ID, SPACE_ID));

        verify(useCase).create(USER_ID, new CreateSpaceCommand("Equipo", "Entrega final"));
        verify(useCase).update(USER_ID, SPACE_ID, new UpdateSpaceCommand("Equipo", "Entrega final"));
        verify(useCase).delete(USER_ID, SPACE_ID);
        verify(useCase).invite(USER_ID, SPACE_ID,
                new InviteMemberCommand("equipo@nexo.test", SpaceMember.Role.MIEMBRO));
    }
}
