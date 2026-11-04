package com.nexo.domain.model;

import java.util.List;

/** Respuesta breve de la asistencia inteligente aplicada al contexto de tareas del usuario. */
public record TaskAssistantSuggestion(
        String answer,
        List<String> recommendedActions,
        boolean providerConfigured,
        List<AssistantActionProposal> proposedActions
) {
    public TaskAssistantSuggestion {
        recommendedActions = recommendedActions == null ? List.of() : List.copyOf(recommendedActions);
        proposedActions = proposedActions == null ? List.of() : proposedActions.stream()
                .filter(AssistantActionProposal::isUsable).limit(3).toList();
    }
}
