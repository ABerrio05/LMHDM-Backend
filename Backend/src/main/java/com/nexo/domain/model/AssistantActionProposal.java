package com.nexo.domain.model;

import java.util.Locale;

/** Acción que la IA propone; nunca se ejecuta sin la confirmación del usuario. */
public record AssistantActionProposal(
        Type type,
        String title,
        String description,
        String dueDate,
        String priority
) {
    public enum Type { TASK, NOTE }

    public AssistantActionProposal {
        title = title == null ? "" : title.trim();
        description = description == null ? "" : description.trim();
        dueDate = dueDate == null ? "" : dueDate.trim();
        priority = priority == null ? "MEDIA" : priority.trim().toUpperCase(Locale.ROOT);
    }

    public boolean isUsable() { return type != null && !title.isBlank(); }
}
