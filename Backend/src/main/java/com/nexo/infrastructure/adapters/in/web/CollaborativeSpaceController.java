package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.ports.in.CollaborativeSpaceUseCase;
import com.nexo.domain.model.CollaborativeSpace;
import com.nexo.domain.model.SpaceMember;
import com.nexo.domain.model.Task;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spaces")
@Tag(name = "Espacios colaborativos", description = "Espacios, integrantes y tareas compartidas")
@SecurityRequirement(name = "bearerAuth")
public class CollaborativeSpaceController {
    private final CollaborativeSpaceUseCase spaces;
    public CollaborativeSpaceController(CollaborativeSpaceUseCase spaces) { this.spaces = spaces; }

    @PostMapping
    @Operation(summary = "Crear espacio colaborativo")
    public ResponseEntity<CollaborativeSpace> create(@AuthenticationPrincipal Long userId,
                                                       @Valid @RequestBody CreateSpaceRequest request) {
        CollaborativeSpace created = spaces.create(userId,
                new CollaborativeSpaceUseCase.CreateSpaceCommand(request.name(), request.description()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @GetMapping
    @Operation(summary = "Listar mis espacios")
    public List<CollaborativeSpace> list(@AuthenticationPrincipal Long userId) { return spaces.listMine(userId); }
    @GetMapping("/{spaceId}")
    @Operation(summary = "Consultar espacio")
    public CollaborativeSpace get(@AuthenticationPrincipal Long userId, @PathVariable Long spaceId) {
        return spaces.get(userId, spaceId);
    }
    @PutMapping("/{spaceId}")
    @Operation(summary = "Editar espacio colaborativo")
    public CollaborativeSpace update(@AuthenticationPrincipal Long userId, @PathVariable Long spaceId,
                                     @Valid @RequestBody CreateSpaceRequest request) {
        return spaces.update(userId, spaceId,
                new CollaborativeSpaceUseCase.UpdateSpaceCommand(request.name(), request.description()));
    }
    @DeleteMapping("/{spaceId}")
    @Operation(summary = "Eliminar espacio y conservar sus tareas como personales")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long spaceId) {
        spaces.delete(userId, spaceId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{spaceId}/members")
    @Operation(summary = "Invitar integrante")
    public ResponseEntity<CollaborativeSpaceUseCase.InvitationResult> invite(@AuthenticationPrincipal Long userId, @PathVariable Long spaceId,
                                               @Valid @RequestBody InviteMemberRequest request) {
        var result = spaces.invite(userId, spaceId,
                new CollaborativeSpaceUseCase.InviteMemberCommand(request.email(), request.role()));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    @GetMapping("/{spaceId}/members")
    @Operation(summary = "Listar integrantes")
    public List<MemberResponse> members(@AuthenticationPrincipal Long userId,
                                                                          @PathVariable Long spaceId) {
        return spaces.listMemberViews(userId, spaceId).stream()
                .map(member -> new MemberResponse(member.id(), member.userId(), member.userName(), member.role(), member.joinedAt()))
                .toList();
    }
    @PutMapping("/{spaceId}/members/{memberUserId}/role")
    @Operation(summary = "Modificar rol de integrante")
    public SpaceMember updateRole(@AuthenticationPrincipal Long userId, @PathVariable Long spaceId,
                                  @PathVariable Long memberUserId, @Valid @RequestBody UpdateRoleRequest request) {
        return spaces.updateMemberRole(userId, spaceId, memberUserId, request.role());
    }
    @PatchMapping("/{spaceId}/tasks/{taskId}/assignee")
    @Operation(summary = "Asignar tarea a integrante")
    public Task assignTask(@AuthenticationPrincipal Long userId, @PathVariable Long spaceId, @PathVariable Long taskId,
                           @Valid @RequestBody AssignTaskRequest request) {
        return spaces.assignTask(userId, spaceId, taskId, request.assigneeId());
    }
    @GetMapping("/{spaceId}/tasks")
    @Operation(summary = "Listar tareas de un espacio")
    public List<Task> tasks(@AuthenticationPrincipal Long userId, @PathVariable Long spaceId) {
        return spaces.listTasks(userId, spaceId);
    }

    public record CreateSpaceRequest(@NotBlank @Size(max = 150) String name, @Size(max = 500) String description) { }
    public record InviteMemberRequest(@NotBlank @Email String email, @NotNull SpaceMember.Role role) { }
    public record UpdateRoleRequest(@NotNull SpaceMember.Role role) { }
    public record AssignTaskRequest(@NotNull Long assigneeId) { }
    public record MemberResponse(Long id, Long userId, String userName, SpaceMember.Role role,
                                 java.time.LocalDateTime joinedAt) { }
}
