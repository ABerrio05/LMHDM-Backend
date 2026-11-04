package com.nexo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nexo.application.ports.out.TaskAssistantProviderPort;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.domain.model.Task;
import com.nexo.domain.model.TaskAssistantSuggestion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TaskAssistantServiceTest {
    @Test
    void returnsHelpfulSetupMessageWithoutCallingProviderWhenAiIsNotConfigured() {
        CapturingProvider provider = new CapturingProvider(false, "no debería usarse");
        TaskAssistantService service = new TaskAssistantService(new FixedTaskRepository(List.of(pendingTask())), provider);

        TaskAssistantSuggestion suggestion = service.suggest(7L, "¿Qué debo priorizar?");

        assertFalse(suggestion.providerConfigured());
        assertTrue(suggestion.answer().contains("Plan automático local"));
        assertTrue(suggestion.answer().contains("Entregar avance"));
        assertEquals(List.of("Revisar: Entregar avance (ALTA)"), suggestion.recommendedActions());
        assertFalse(provider.wasCalled);
    }

    @Test
    void sendsOnlyPendingTasksToConfiguredProviderAndReturnsItsAnswer() {
        CapturingProvider provider = new CapturingProvider(true, "Prioriza Entregar avance hoy.");
        Task completed = new Task(2L, "Ya terminada", "", null, Task.Priority.BAJA,
                Task.TaskStatus.COMPLETADA, 7L, null, null);
        TaskAssistantService service = new TaskAssistantService(
                new FixedTaskRepository(List.of(pendingTask(), completed)), provider);

        TaskAssistantSuggestion suggestion = service.suggest(7L, "¿Qué debo priorizar?");

        assertTrue(suggestion.providerConfigured());
        assertEquals("Prioriza Entregar avance hoy.", suggestion.answer());
        assertEquals("¿Qué debo priorizar?", provider.question);
        assertEquals(1, provider.context.size());
        assertTrue(provider.context.getFirst().contains("Entregar avance"));
        assertFalse(provider.context.getFirst().contains("Ya terminada"));
    }

    private static Task pendingTask() {
        return new Task(1L, "Entregar avance", "", LocalDateTime.of(2026, 11, 2, 10, 0),
                Task.Priority.ALTA, Task.TaskStatus.PENDIENTE, 7L, null, null);
    }

    private static final class FixedTaskRepository implements TaskRepositoryPort {
        private final List<Task> tasks;
        private FixedTaskRepository(List<Task> tasks) { this.tasks = tasks; }
        @Override public List<Task> findAllByOwnerId(Long ownerId) { return tasks; }
        @Override public Task save(Task task) { throw new UnsupportedOperationException(); }
        @Override public List<Task> findAllBySpaceId(Long spaceId) { return List.of(); }
        @Override public Optional<Task> findById(Long taskId) { return Optional.empty(); }
        @Override public void delete(Task task) { }
    }

    private static final class CapturingProvider implements TaskAssistantProviderPort {
        private final boolean configured;
        private final String response;
        private boolean wasCalled;
        private String question;
        private List<String> context = List.of();

        private CapturingProvider(boolean configured, String response) {
            this.configured = configured;
            this.response = response;
        }

        @Override public boolean isConfigured() { return configured; }
        @Override public TaskAssistantProviderPort.AssistantResponse ask(String question, List<String> taskContext) {
            this.wasCalled = true;
            this.question = question;
            this.context = taskContext;
            return new TaskAssistantProviderPort.AssistantResponse(response, List.of());
        }
    }
}
