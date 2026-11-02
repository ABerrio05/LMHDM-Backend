package com.nexo.application.ports.out;

import java.util.List;

/** Puerto para cualquier proveedor externo compatible con chat completions. */
public interface TaskAssistantProviderPort {
    boolean isConfigured();
    String ask(String question, List<String> taskContext);
}
