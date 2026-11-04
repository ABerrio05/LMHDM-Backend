package com.nexo.application.ports.out;

import java.util.List;
import com.nexo.domain.model.AssistantActionProposal;

/** Puerto para cualquier proveedor externo compatible con chat completions. */
public interface TaskAssistantProviderPort {
    boolean isConfigured();
    AssistantResponse ask(String question, List<String> taskContext);

    record AssistantResponse(String answer, List<AssistantActionProposal> proposedActions) {
        public AssistantResponse {
            proposedActions = proposedActions == null ? List.of() : List.copyOf(proposedActions);
        }
    }
}
