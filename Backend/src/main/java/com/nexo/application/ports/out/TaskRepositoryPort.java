package com.nexo.application.ports.out;

import com.nexo.domain.model.Task;
import java.util.List;
import java.util.Optional;

public interface TaskRepositoryPort {
    Task save(Task task);
    List<Task> findAllByOwnerId(Long ownerId);
    Optional<Task> findById(Long taskId);
    void delete(Task task);
}
