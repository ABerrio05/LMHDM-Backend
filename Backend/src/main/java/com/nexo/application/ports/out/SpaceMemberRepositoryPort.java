package com.nexo.application.ports.out;

import com.nexo.domain.model.SpaceMember;
import java.util.List;
import java.util.Optional;

public interface SpaceMemberRepositoryPort {
    SpaceMember save(SpaceMember member);
    Optional<SpaceMember> findBySpaceIdAndUserId(Long spaceId, Long userId);
    List<SpaceMember> findAllBySpaceId(Long spaceId);
    List<SpaceMember> findAllByUserId(Long userId);
}
