package com.nexo.application.service;

import com.nexo.application.ports.in.TaskAssistantUseCase;
import com.nexo.application.ports.out.TaskAssistantProviderPort;
import com.nexo.application.ports.out.TaskRepositoryPort;
import com.nexo.application.ports.out.PersonalNoteRepositoryPort;
import com.nexo.domain.model.PersonalNote;
import com.nexo.domain.model.Task;
import com.nexo.domain.model.TaskAssistantSuggestion;
import com.nexo.domain.model.AssistantActionProposal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Construye contexto mínimo de las tareas y delega la respuesta al proveedor de IA configurado. */
public class TaskAssistantService implements TaskAssistantUseCase {
    private static final Logger log = LoggerFactory.getLogger(TaskAssistantService.class);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Pattern NOTE_CONTENT_UPDATE = Pattern.compile(
            "(?isu)(?:descripci[oó]n|contenido).*?(?:\\bpor\\b|\\ba\\b)\\s*(?:esta\\s*)?:?\\s*(.+)$");
    private final TaskRepositoryPort tasks;
    private final TaskAssistantProviderPort provider;
    private final PersonalNoteRepositoryPort notes;

    public TaskAssistantService(TaskRepositoryPort tasks, PersonalNoteRepositoryPort notes, TaskAssistantProviderPort provider) {
        this.tasks = tasks;
        this.notes = notes;
        this.provider = provider;
    }

    @Override
    public TaskAssistantSuggestion suggest(Long userId, String question) {
        List<Task> userTasks = tasks.findAllByOwnerId(userId).stream()
                .sorted(Comparator.comparing(Task::dueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(20)
                .toList();
        List<Task> pending = userTasks.stream().filter(task -> task.status() != Task.TaskStatus.COMPLETADA).toList();
        List<PersonalNote> userNotes = notes.findAllByOwnerId(userId).stream().limit(20).toList();
        List<String> context = new java.util.ArrayList<>();
        userTasks.forEach(task -> context.add(summarize(task)));
        userNotes.forEach(note -> context.add(summarize(note)));

        if (!provider.isConfigured()) {
            log.info("assistant_requested_without_provider userId={} pendingTasks={}", userId, pending.size());
            return new TaskAssistantSuggestion(
                    buildLocalPlan(pending),
                    localActions(pending), false, List.of());
        }

        TaskAssistantProviderPort.AssistantResponse response = provider.ask(question, context);
        List<AssistantActionProposal> actions = response.proposedActions().isEmpty()
                ? recoverNoteUpdate(question, userNotes)
                : response.proposedActions();
        log.info("assistant_response_generated userId={} pendingTasks={}", userId, pending.size());
        return new TaskAssistantSuggestion(response.answer(), localActions(pending), true, actions);
    }

    private String summarize(Task task) {
        String due = task.dueDate() == null ? "sin fecha" : task.dueDate().format(DATE);
        return "TAREA id=" + task.id() + ": " + task.title() + " | prioridad: " + task.priority() + " | estado: " + task.status() + " | vence: " + due;
    }

    private String summarize(PersonalNote note) {
        return "NOTA id=" + note.id() + ": " + note.title() + " | contenido: " + note.content();
    }

    private List<String> localActions(List<Task> pending) {
        if (pending.isEmpty()) return List.of("No tienes tareas pendientes. Puedes planear tu próxima meta.");
        return pending.stream().limit(3)
                .map(task -> "Revisar: " + task.title() + " (" + task.priority() + ")")
                .toList();
    }

    /**
     * Gemini puede describir correctamente una edición pero omitir el JSON de la acción.
     * Para la edición explícita de una nota recuperamos solo el objetivo que coincide con
     * el título real del usuario; si no hay coincidencia exacta, no se propone nada.
     */
    private List<AssistantActionProposal> recoverNoteUpdate(String question, List<PersonalNote> userNotes) {
        String normalizedQuestion = normalize(question);
        boolean requestsUpdate = normalizedQuestion.matches(".*(edita|editar|actualiza|actualizar|modifica|modificar|cambia|cambiar).*");
        boolean mentionsNote = normalizedQuestion.contains("nota");
        if (!requestsUpdate || !mentionsNote) return List.of();

        Matcher contentMatcher = NOTE_CONTENT_UPDATE.matcher(question.trim());
        if (!contentMatcher.find()) return List.of();
        String newContent = contentMatcher.group(1).trim();
        if (newContent.isBlank()) return List.of();

        return userNotes.stream()
                .filter(note -> normalizedQuestion.contains(normalize(note.title())))
                .findFirst()
                .map(note -> List.of(new AssistantActionProposal(
                        AssistantActionProposal.Type.NOTE_UPDATE, note.id(), note.title(), newContent, "", "MEDIA")))
                .orElseGet(List::of);
    }

    private String normalize(String value) {
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    /**
     * Mantiene útil la pantalla cuando el proveedor generativo no está disponible.
     * No se presenta como una respuesta de IA: aplica una regla explicable de
     * prioridad y fecha límite sobre los datos que ya pertenecen al usuario.
     */
    private String buildLocalPlan(List<Task> pending) {
        if (pending.isEmpty()) {
            return "No tienes tareas pendientes. Puedes definir una nueva meta o revisar tus recordatorios.";
        }
        Task first = pending.getFirst();
        String due = first.dueDate() == null ? "no tiene fecha límite" : "vence el " + first.dueDate().format(DATE);
        return "Plan automático local: comienza por ‘" + first.title() + "’ porque tiene prioridad "
                + first.priority() + " y " + due + ". "
                + "Para una respuesta generativa, el despliegue puede activar el proveedor de IA mediante variables seguras.";
    }
}
