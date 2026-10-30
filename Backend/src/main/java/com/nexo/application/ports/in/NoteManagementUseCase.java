package com.nexo.application.ports.in;

import com.nexo.domain.model.PersonalNote;
import java.util.List;

public interface NoteManagementUseCase {
    PersonalNote create(Long ownerId, CreateNoteCommand command);
    List<PersonalNote> listMine(Long ownerId);
    PersonalNote update(Long ownerId, Long noteId, UpdateNoteCommand command);
    void delete(Long ownerId, Long noteId);

    record CreateNoteCommand(String title, String content) { }
    record UpdateNoteCommand(String title, String content) { }
}
