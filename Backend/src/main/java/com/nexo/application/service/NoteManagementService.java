package com.nexo.application.service;

import com.nexo.application.ports.in.NoteManagementUseCase;
import com.nexo.application.ports.out.PersonalNoteRepositoryPort;
import com.nexo.domain.model.PersonalNote;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoteManagementService implements NoteManagementUseCase {
    private static final Logger log = LoggerFactory.getLogger(NoteManagementService.class);
    private final PersonalNoteRepositoryPort notes;

    public NoteManagementService(PersonalNoteRepositoryPort notes) {
        this.notes = notes;
    }

    @Override
    public PersonalNote create(Long ownerId, CreateNoteCommand command) {
        PersonalNote note = notes.save(new PersonalNote(null, command.title(), command.content(), ownerId, LocalDateTime.now()));
        log.info("note_created noteId={} ownerId={}", note.id(), ownerId);
        return note;
    }

    @Override
    public List<PersonalNote> listMine(Long ownerId) {
        return notes.findAllByOwnerId(ownerId);
    }

    @Override
    public PersonalNote update(Long ownerId, Long noteId, UpdateNoteCommand command) {
        PersonalNote current = ownedBy(ownerId, noteId);
        PersonalNote note = notes.save(new PersonalNote(current.id(), command.title(), command.content(), current.ownerId(), current.createdAt()));
        log.info("note_updated noteId={} ownerId={}", noteId, ownerId);
        return note;
    }

    @Override
    public void delete(Long ownerId, Long noteId) {
        notes.delete(ownedBy(ownerId, noteId));
        log.info("note_deleted noteId={} ownerId={}", noteId, ownerId);
    }

    private PersonalNote ownedBy(Long ownerId, Long noteId) {
        PersonalNote note = notes.findById(noteId).orElseThrow(NoteNotFoundException::new);
        if (!note.ownerId().equals(ownerId)) {
            throw new NoteAccessDeniedException();
        }
        return note;
    }

    public static class NoteNotFoundException extends RuntimeException { }
    public static class NoteAccessDeniedException extends RuntimeException { }
}
