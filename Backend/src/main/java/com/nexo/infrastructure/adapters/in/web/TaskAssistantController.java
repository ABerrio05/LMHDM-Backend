package com.nexo.infrastructure.adapters.in.web;

import com.nexo.application.ports.in.TaskAssistantUseCase;
import com.nexo.domain.model.TaskAssistantSuggestion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
@Tag(name = "Asistente", description = "Asistencia inteligente basada en tareas del usuario")
@SecurityRequirement(name = "bearerAuth")
public class TaskAssistantController {
    private final TaskAssistantUseCase assistant;

    public TaskAssistantController(TaskAssistantUseCase assistant) { this.assistant = assistant; }

    @PostMapping("/suggestions")
    @Operation(summary = "Solicitar sugerencias para organizar mis tareas")
    public TaskAssistantSuggestion suggest(@AuthenticationPrincipal Long userId, @Valid @RequestBody AssistantRequest request) {
        return assistant.suggest(userId, request.question());
    }

    public record AssistantRequest(
            @NotBlank(message = "La consulta es obligatoria")
            @Size(max = 800, message = "La consulta no puede superar 800 caracteres")
            String question) { }
}
