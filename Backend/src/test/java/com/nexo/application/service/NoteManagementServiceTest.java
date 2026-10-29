package com.nexo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nexo.application.ports.in.NoteManagementUseCase.CreateNoteCommand;
import com.nexo.application.ports.in.NoteManagementUseCase.UpdateNoteCommand;
import com.nexo.application.ports.out.PersonalNoteRepositoryPort;
import com.nexo.domain.model.PersonalNote;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NoteManagementServiceTest {
    private InMemoryNoteRepository notes;
    private NoteManagementService service;

    @BeforeEach
    void setUp() {
        notes = new InMemoryNoteRepository();
        service = new NoteManagementService(notes);
    }

    @Test
    void createsAndListsOnlyTheAuthenticatedUsersNotes() {
        PersonalNote created = service.create(7L, new CreateNoteCommand("Revisión", "Preparar las pruebas"));
        service.create(8L, new CreateNoteCommand("Privada", "No debe aparecer"));

        assertEquals(1L, created.id());
        assertEquals(List.of(created), service.listMine(7L));
    }

    @Test
    void updatesAndDeletesAnOwnedNote() {
        PersonalNote created = service.create(7L, new CreateNoteCommand("Inicial", "Contenido inicial"));
        PersonalNote updated = service.update(7L, created.id(), new UpdateNoteCommand("Actualizada", "Contenido nuevo"));
        service.delete(7L, created.id());

        assertEquals("Actualizada", updated.title());
        assertEquals(0, notes.data.size());
    }

    @Test
    void preventsAnotherUserFromChangingOrDeletingANote() {
        PersonalNote created = service.create(7L, new CreateNoteCommand("Propia", "Contenido"));

        assertThrows(NoteManagementService.NoteAccessDeniedException.class,
                () -> service.update(8L, created.id(), new UpdateNoteCommand("No", "No")));
        assertThrows(NoteManagementService.NoteAccessDeniedException.class,
                () -> service.delete(8L, created.id()));
    }

    @Test
    void reportsMissingNote() {
        assertThrows(NoteManagementService.NoteNotFoundException.class,
                () -> service.delete(7L, 99L));
    }

    private static final class InMemoryNoteRepository implements PersonalNoteRepositoryPort {
        private final List<PersonalNote> data = new ArrayList<>();

        @Override public PersonalNote save(PersonalNote note) {
            PersonalNote saved = new PersonalNote(note.id() == null ? (long) data.size() + 1 : note.id(), note.title(),
                    note.content(), note.ownerId(), note.createdAt() == null ? LocalDateTime.now() : note.createdAt());
            data.removeIf(current -> current.id().equals(saved.id()));
            data.add(saved);
            return saved;
        }
        @Override public List<PersonalNote> findAllByOwnerId(Long ownerId) {
            return data.stream().filter(note -> note.ownerId().equals(ownerId)).toList();
        }
        @Override public Optional<PersonalNote> findById(Long noteId) {
            return data.stream().filter(note -> note.id().equals(noteId)).findFirst();
        }
        @Override public void delete(PersonalNote note) { data.removeIf(current -> current.id().equals(note.id())); }
    }
}
