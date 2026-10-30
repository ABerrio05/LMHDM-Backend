package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.ports.in.NoteManagementUseCase;
import com.nexo.domain.model.PersonalNote;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes")
@Tag(name = "Notas", description = "Notas personales del usuario")
@SecurityRequirement(name = "bearerAuth")
public class PersonalNoteController {
    private final NoteManagementUseCase notes;
    public PersonalNoteController(NoteManagementUseCase notes) { this.notes = notes; }
    @GetMapping @Operation(summary = "Listar mis notas")
    public List<PersonalNote> list(@AuthenticationPrincipal Long userId) { return notes.listMine(userId); }
    @PostMapping @Operation(summary = "Crear nota")
    public ResponseEntity<PersonalNote> create(@AuthenticationPrincipal Long userId, @Valid @RequestBody NoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notes.create(userId, new NoteManagementUseCase.CreateNoteCommand(request.title(), request.content())));
    }
    @PutMapping("/{noteId}") @Operation(summary = "Editar nota")
    public PersonalNote update(@AuthenticationPrincipal Long userId, @PathVariable Long noteId, @Valid @RequestBody NoteRequest request) {
        return notes.update(userId, noteId, new NoteManagementUseCase.UpdateNoteCommand(request.title(), request.content()));
    }
    @DeleteMapping("/{noteId}") @Operation(summary = "Eliminar nota")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long noteId) {
        notes.delete(userId, noteId); return ResponseEntity.noContent().build();
    }
    public record NoteRequest(@NotBlank @Size(max = 255) String title, @NotBlank @Size(max = 10_000) String content) { }
}
