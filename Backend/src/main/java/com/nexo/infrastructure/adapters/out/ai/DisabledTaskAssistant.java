package com.nexo.infrastructure.adapters.out.ai;

import com.nexo.application.ports.out.TaskAssistantProviderPort;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledTaskAssistant implements TaskAssistantProviderPort {
    @Override public boolean isConfigured() { return false; }
    @Override public AssistantResponse ask(String question, List<String> taskContext) {
        throw new IllegalStateException("El proveedor de IA no está configurado");
    }
}
