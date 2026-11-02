package com.nexo.domain.model;

import java.util.List;

/** Respuesta breve de la asistencia inteligente aplicada al contexto de tareas del usuario. */
public record TaskAssistantSuggestion(
        String answer,
        List<String> recommendedActions,
        boolean providerConfigured
) {
    public TaskAssistantSuggestion {
        recommendedActions = recommendedActions == null ? List.of() : List.copyOf(recommendedActions);
    }
}
