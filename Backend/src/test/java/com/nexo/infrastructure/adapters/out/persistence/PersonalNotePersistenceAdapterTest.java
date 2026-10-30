package com.nexo.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexo.domain.model.PersonalNote;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PersonalNotePersistenceAdapterTest {
    private static final Long OWNER_ID = 7L;
    private static final Long NOTE_ID = 13L;

    @Test
    void savesNewNotesAndMapsThemBackToTheDomain() {
        SpringDataPersonalNoteRepository repository = mock(SpringDataPersonalNoteRepository.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        PersonalNotePersistenceAdapter adapter = new PersonalNotePersistenceAdapter(repository);
        PersonalNote note = new PersonalNote(null, "Título", "Contenido", OWNER_ID, LocalDateTime.of(2026, 10, 29, 8, 0));

        PersonalNote saved = adapter.save(note);

        assertEquals("Título", saved.title());
        assertEquals("Contenido", saved.content());
        assertEquals(OWNER_ID, saved.ownerId());
    }

    @Test
    void updatesExistingNotesAndSupportsReadAndDeleteOperations() {
        SpringDataPersonalNoteRepository repository = mock(SpringDataPersonalNoteRepository.class);
        PersonalNoteJpaEntity entity = new PersonalNoteJpaEntity("Antes", "Contenido anterior", OWNER_ID, LocalDateTime.of(2026, 10, 28, 8, 0));
        when(repository.findById(NOTE_ID)).thenReturn(Optional.of(entity));
        when(repository.findAllByOwnerIdOrderByCreatedAtDesc(OWNER_ID)).thenReturn(List.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        PersonalNotePersistenceAdapter adapter = new PersonalNotePersistenceAdapter(repository);
        PersonalNote updated = new PersonalNote(NOTE_ID, "Después", "Contenido nuevo", OWNER_ID, LocalDateTime.of(2026, 10, 29, 8, 0));

        assertEquals("Después", adapter.save(updated).title());
        assertEquals("Contenido nuevo", entity.getContent());
        assertEquals(1, adapter.findAllByOwnerId(OWNER_ID).size());
        assertFalse(adapter.findById(NOTE_ID).isEmpty());
        adapter.delete(updated);

        verify(repository).save(entity);
        verify(repository).deleteById(NOTE_ID);
    }
}
