package com.nexo.application.ports.out;

import com.nexo.domain.model.CollaborativeSpace;
import java.util.List;
import java.util.Optional;

public interface CollaborativeSpaceRepositoryPort {
    CollaborativeSpace save(CollaborativeSpace space);
    Optional<CollaborativeSpace> findById(Long spaceId);
    List<CollaborativeSpace> findAllByIds(List<Long> spaceIds);
}
