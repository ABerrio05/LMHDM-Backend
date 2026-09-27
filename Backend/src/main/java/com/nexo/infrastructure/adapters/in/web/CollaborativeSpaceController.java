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
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spaces")
public class CollaborativeSpaceController {
    private final CollaborativeSpaceUseCase spaces;
    public CollaborativeSpaceController(CollaborativeSpaceUseCase spaces) { this.spaces = spaces; }

    @PostMapping
    public ResponseEntity<CollaborativeSpace> create(@AuthenticationPrincipal Long userId,
                                                       @Valid @RequestBody CreateSpaceRequest request) {
        CollaborativeSpace created = spaces.create(userId,
                new CollaborativeSpaceUseCase.CreateSpaceCommand(request.name(), request.description()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @GetMapping public List<CollaborativeSpace> list(@AuthenticationPrincipal Long userId) { return spaces.listMine(userId); }
    @GetMapping("/{spaceId}") public CollaborativeSpace get(@AuthenticationPrincipal Long userId, @PathVariable Long spaceId) {
        return spaces.get(userId, spaceId);
    }
    @PostMapping("/{spaceId}/members")
    public ResponseEntity<SpaceMember> invite(@AuthenticationPrincipal Long userId, @PathVariable Long spaceId,
                                               @Valid @RequestBody InviteMemberRequest request) {
        SpaceMember member = spaces.invite(userId, spaceId,
                new CollaborativeSpaceUseCase.InviteMemberCommand(request.email(), request.role()));
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }
    @GetMapping("/{spaceId}/members") public List<SpaceMember> members(@AuthenticationPrincipal Long userId,
                                                                          @PathVariable Long spaceId) {
        return spaces.listMembers(userId, spaceId);
    }
    @GetMapping("/{spaceId}/tasks") public List<Task> tasks(@AuthenticationPrincipal Long userId, @PathVariable Long spaceId) {
        return spaces.listTasks(userId, spaceId);
    }

    public record CreateSpaceRequest(@NotBlank @Size(max = 150) String name, @Size(max = 500) String description) { }
    public record InviteMemberRequest(@NotBlank @Email String email, @NotNull SpaceMember.Role role) { }
}
