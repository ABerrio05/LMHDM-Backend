package com.nexo.application.ports.out;

import com.nexo.domain.model.PersonalNote;
import java.util.List;
import java.util.Optional;

public interface PersonalNoteRepositoryPort {
    PersonalNote save(PersonalNote note);
    List<PersonalNote> findAllByOwnerId(Long ownerId);
    Optional<PersonalNote> findById(Long noteId);
    void delete(PersonalNote note);
}
