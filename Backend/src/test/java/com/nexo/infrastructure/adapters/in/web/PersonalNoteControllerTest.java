package com.nexo.infrastructure.adapters.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexo.application.ports.in.NoteManagementUseCase;
import com.nexo.domain.model.PersonalNote;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class PersonalNoteControllerTest {
    private static final Long OWNER_ID = 7L;
    private static final Long NOTE_ID = 13L;

    @Test
    void delegatesAllNoteOperationsToTheUseCase() {
        NoteManagementUseCase useCase = mock(NoteManagementUseCase.class);
        PersonalNote note = new PersonalNote(NOTE_ID, "Preparación", "Revisar pruebas", OWNER_ID, LocalDateTime.now());
        when(useCase.listMine(OWNER_ID)).thenReturn(List.of(note));
        when(useCase.create(eq(OWNER_ID), any())).thenReturn(note);
        when(useCase.update(eq(OWNER_ID), eq(NOTE_ID), any())).thenReturn(note);
        PersonalNoteController controller = new PersonalNoteController(useCase);
        PersonalNoteController.NoteRequest request = new PersonalNoteController.NoteRequest("Preparación", "Revisar pruebas");

        assertEquals(List.of(note), controller.list(OWNER_ID));
        var created = controller.create(OWNER_ID, request);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertEquals(note, created.getBody());
        assertEquals(note, controller.update(OWNER_ID, NOTE_ID, request));
        var deleted = controller.delete(OWNER_ID, NOTE_ID);
        assertEquals(HttpStatus.NO_CONTENT, deleted.getStatusCode());
        assertNull(deleted.getBody());

        verify(useCase).listMine(OWNER_ID);
        verify(useCase).create(eq(OWNER_ID), eq(new NoteManagementUseCase.CreateNoteCommand("Preparación", "Revisar pruebas")));
        verify(useCase).update(eq(OWNER_ID), eq(NOTE_ID), eq(new NoteManagementUseCase.UpdateNoteCommand("Preparación", "Revisar pruebas")));
        verify(useCase).delete(OWNER_ID, NOTE_ID);
    }
}
