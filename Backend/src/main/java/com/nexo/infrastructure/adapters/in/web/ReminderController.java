package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.ports.in.ReminderManagementUseCase;
import com.nexo.domain.model.Reminder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
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
@RequestMapping("/api")
@Tag(name = "Recordatorios", description = "Programación de recordatorios para tareas")
@SecurityRequirement(name = "bearerAuth")
public class ReminderController {
    private final ReminderManagementUseCase reminders;
    public ReminderController(ReminderManagementUseCase reminders) { this.reminders = reminders; }

    @PostMapping("/tasks/{taskId}/reminders")
    @Operation(summary = "Crear recordatorio")
    public ResponseEntity<Reminder> create(@AuthenticationPrincipal Long userId, @PathVariable Long taskId,
                                           @Valid @RequestBody ReminderRequest request) {
        Reminder reminder = reminders.create(userId, taskId,
                new ReminderManagementUseCase.CreateReminderCommand(request.scheduledAt(), request.message()));
        return ResponseEntity.status(HttpStatus.CREATED).body(reminder);
    }
    @GetMapping("/tasks/{taskId}/reminders")
    @Operation(summary = "Listar recordatorios de una tarea")
    public List<Reminder> list(@AuthenticationPrincipal Long userId, @PathVariable Long taskId) {
        return reminders.listForTask(userId, taskId);
    }
    @PutMapping("/reminders/{reminderId}")
    @Operation(summary = "Editar recordatorio")
    public Reminder update(@AuthenticationPrincipal Long userId, @PathVariable Long reminderId,
                           @Valid @RequestBody ReminderRequest request) {
        return reminders.update(userId, reminderId,
                new ReminderManagementUseCase.UpdateReminderCommand(request.scheduledAt(), request.message()));
    }
    @DeleteMapping("/reminders/{reminderId}")
    @Operation(summary = "Eliminar recordatorio")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long reminderId) {
        reminders.delete(userId, reminderId); return ResponseEntity.noContent().build();
    }
    public record ReminderRequest(@NotNull LocalDateTime scheduledAt, @NotBlank @Size(max = 500) String message) { }
}
