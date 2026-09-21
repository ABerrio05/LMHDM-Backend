package com.nexo.application.ports.in;

import com.nexo.domain.model.Task;

public interface CreateTaskUseCase {
    Task create(CreateTaskCommand command);

    record CreateTaskCommand(String title, String description, Long ownerId) { }
}
