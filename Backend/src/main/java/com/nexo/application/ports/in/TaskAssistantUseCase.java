package com.nexo.application.ports.in;

import com.nexo.domain.model.TaskAssistantSuggestion;

public interface TaskAssistantUseCase {
    TaskAssistantSuggestion suggest(Long userId, String question);
}
