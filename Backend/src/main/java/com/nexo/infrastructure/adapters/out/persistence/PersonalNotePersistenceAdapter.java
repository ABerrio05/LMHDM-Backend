package com.nexo.infrastructure.adapters.out.persistence;

import com.nexo.application.ports.out.PersonalNoteRepositoryPort;
import com.nexo.domain.model.PersonalNote;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PersonalNotePersistenceAdapter implements PersonalNoteRepositoryPort {
    private final SpringDataPersonalNoteRepository repository;
    public PersonalNotePersistenceAdapter(SpringDataPersonalNoteRepository repository) { this.repository = repository; }
    @Override public PersonalNote save(PersonalNote note) {
        PersonalNoteJpaEntity entity = new PersonalNoteJpaEntity(note.title(), note.content(), note.ownerId(), note.createdAt());
        if (note.id() != null) {
            PersonalNoteJpaEntity existing = repository.findById(note.id()).orElseThrow();
            existing.update(note.title(), note.content());
            entity = existing;
        }
        return toDomain(repository.save(entity));
    }
    @Override public List<PersonalNote> findAllByOwnerId(Long ownerId) {
        return repository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(this::toDomain).toList();
    }
    @Override public Optional<PersonalNote> findById(Long noteId) { return repository.findById(noteId).map(this::toDomain); }
    @Override public void delete(PersonalNote note) { repository.deleteById(note.id()); }
    private PersonalNote toDomain(PersonalNoteJpaEntity entity) {
        return new PersonalNote(entity.getId(), entity.getTitle(), entity.getContent(), entity.getOwnerId(), entity.getCreatedAt());
    }
}
