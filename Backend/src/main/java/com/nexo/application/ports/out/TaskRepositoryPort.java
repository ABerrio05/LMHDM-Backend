package com.nexo.application.ports.out;

import com.nexo.domain.model.Task;

public interface TaskRepositoryPort {
    Task save(Task task);
}
