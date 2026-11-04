package com.nexo.domain.model;

import java.util.Locale;

/** Acción que la IA propone; nunca se ejecuta sin la confirmación del usuario. */
public record AssistantActionProposal(
        Type type,
        Long targetId,
        String title,
        String description,
        String dueDate,
        String priority
) {
    public enum Type { TASK_CREATE, NOTE_CREATE, TASK_UPDATE, NOTE_UPDATE, TASK_DELETE, NOTE_DELETE }

    public AssistantActionProposal {
        title = title == null ? "" : title.trim();
        description = description == null ? "" : description.trim();
        dueDate = dueDate == null ? "" : dueDate.trim();
        priority = priority == null ? "MEDIA" : priority.trim().toUpperCase(Locale.ROOT);
    }

    public boolean isUsable() {
        if (type == null) return false;
        return switch (type) {
            case TASK_CREATE, NOTE_CREATE -> !title.isBlank();
            case TASK_UPDATE, NOTE_UPDATE, TASK_DELETE, NOTE_DELETE -> targetId != null && targetId > 0;
        };
    }
}
